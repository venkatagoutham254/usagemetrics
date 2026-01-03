# EC2 Troubleshooting Guide - Billable Metrics Service

## Problem: Application Fails with "Password Authentication Failed"

### Root Cause
**This is NOT an AWS issue.** The problem is a **database initialization timing issue**:
- PostgreSQL container starts but takes 5-10 seconds to be ready
- Application tries to connect immediately via Liquibase
- Connection fails because PostgreSQL isn't accepting connections yet
- Container restarts repeatedly, hitting the same issue

### Solution Applied
Updated `docker-compose.yml` with:
1. **PostgreSQL healthcheck** - Ensures database is ready before app starts
2. **Volume persistence** - Database data survives container restarts
3. **Proper dependency** - Changed from `service_started` to `service_healthy`

---

## EC2 Deployment Steps

### 1. Upload Updated Files to EC2
```bash
# On your local machine, copy files to EC2
scp docker-compose.yml Dockerfile app.jar deploy-ec2.sh root@<EC2-IP>:/root/ec2-user/

# Make deployment script executable
ssh root@<EC2-IP>
cd /root/ec2-user
chmod +x deploy-ec2.sh
```

### 2. Clean Existing Deployment
```bash
# SSH into EC2
ssh root@<EC2-IP>
cd /root/ec2-user

# Stop all containers
docker-compose down

# IMPORTANT: Remove corrupted volumes (this deletes database data)
docker volume ls
docker volume rm ec2-user_postgres_data 2>/dev/null || true

# Remove old containers and images
docker system prune -af --volumes
```

### 3. Deploy Using Script
```bash
./deploy-ec2.sh
```

### 4. Manual Deployment (Alternative)
```bash
# Build and start
docker-compose build --no-cache
docker-compose up -d

# Monitor startup
docker-compose logs -f
```

---

## Diagnostic Commands

### Check Container Status
```bash
docker ps -a
docker-compose ps
```

### Check Database Health
```bash
# Test PostgreSQL is ready
docker exec billable_metrics_pg pg_isready -U postgres -d billable_metrics_db

# Connect to PostgreSQL
docker exec -it billable_metrics_pg psql -U postgres -d billable_metrics_db

# Inside psql, check tables
\dt
\l
```

### View Logs
```bash
# All logs
docker-compose logs -f

# Application only
docker-compose logs -f billable_metrics_app

# Database only
docker-compose logs -f billable_metrics_pg

# Last 100 lines
docker-compose logs --tail=100 billable_metrics_app
```

### Check Network Connectivity
```bash
# From app container to database
docker exec billable_metrics_app ping billable_metrics_pg

# Test database connection from app container
docker exec billable_metrics_app wget --spider -T 5 billable_metrics_pg:5432
```

### Volume Inspection
```bash
# List volumes
docker volume ls

# Inspect postgres volume
docker volume inspect ec2-user_postgres_data
```

---

## Common Issues & Fixes

### Issue 1: "Password Authentication Failed" Still Occurring

**Cause**: Old PostgreSQL data with different credentials

**Fix**:
```bash
docker-compose down
docker volume rm ec2-user_postgres_data
docker-compose up -d
```

### Issue 2: Application Starts Then Crashes

**Cause**: Database healthcheck not working properly

**Verify**:
```bash
# Check database is actually healthy
docker inspect billable_metrics_pg | grep -A 10 Health

# Should show "healthy" status
```

**Fix**:
```bash
# Wait for database to be completely ready
docker-compose down
docker-compose up -d billable_metrics_pg
sleep 30
docker-compose up -d billable_metrics_app
```

### Issue 3: Liquibase Timeout

**Cause**: Database taking longer than expected to initialize

**Fix**: Add connection timeout in `docker-compose.yml` environment:
```yaml
environment:
  - SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=60000
  - SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT=60000
```

### Issue 4: Port Already in Use

**Fix**:
```bash
# Check what's using port 8081
netstat -tulpn | grep 8081

# Kill the process
kill -9 <PID>

# Or change port in docker-compose.yml
ports:
  - "8082:8081"  # Change external port
```

---

## Health Check Endpoints

### Application Health
```bash
curl http://localhost:8081/api/health
```

### Database Connection Test
```bash
docker exec billable_metrics_pg psql -U postgres -d billable_metrics_db -c "SELECT 1;"
```

---

## Performance Monitoring

### Resource Usage
```bash
# Container stats
docker stats

# Specific container
docker stats billable_metrics_app billable_metrics_pg
```

### Memory Issues
If application runs out of memory:
```bash
# Add to docker-compose.yml under billable_metrics_app
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 512M
```

---

## Clean Restart Procedure

```bash
# 1. Stop everything
docker-compose down -v

# 2. Clean Docker system
docker system prune -af --volumes

# 3. Verify cleanup
docker ps -a
docker volume ls

# 4. Rebuild from scratch
docker-compose build --no-cache

# 5. Start services
docker-compose up -d

# 6. Watch logs
docker-compose logs -f
```

---

## Production Recommendations

### 1. Change Default Passwords
```yaml
environment:
  POSTGRES_PASSWORD: ${DB_PASSWORD:-secure_password_here}
```

### 2. Add Resource Limits
```yaml
deploy:
  resources:
    limits:
      cpus: '1'
      memory: 1G
```

### 3. Enable Log Rotation
```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### 4. Add Restart Policy
```yaml
restart: unless-stopped  # Better than 'always'
```

### 5. Monitor Health
Set up external monitoring for:
- `http://<EC2-IP>:8081/api/health`
- Container status
- Disk usage (database volume)

---

## Questions to AWS Team

If the issue persists after these fixes, ask AWS:
1. Are there any security groups blocking internal Docker network?
2. Is there disk I/O throttling on the EBS volume?
3. Are there any EC2 instance limits being hit (CPU credits, memory)?
4. Check CloudWatch logs for any system-level issues

But based on the error logs, **this is a Docker configuration issue, not an AWS infrastructure issue**.
