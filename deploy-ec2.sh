#!/bin/bash
# EC2 Deployment Script for Billable Metrics Service
# This script handles clean deployment with proper database initialization

set -e

echo "================================================"
echo "Billable Metrics Service - EC2 Deployment"
echo "================================================"

# Stop and remove existing containers
echo "Stopping existing containers..."
docker-compose down

# Optional: Clean volumes if you want fresh database (WARNING: Deletes all data)
# Uncomment the next line only if you need to reset the database completely
# docker volume rm usagemetrics_postgres_data 2>/dev/null || true

# Remove old images to force rebuild
echo "Removing old application image..."
docker rmi billable_metrics_app 2>/dev/null || true

# Build and start services
echo "Building application..."
docker-compose build --no-cache

echo "Starting services..."
docker-compose up -d

# Wait for database to be healthy
echo "Waiting for PostgreSQL to be ready..."
timeout=60
counter=0
until docker exec billable_metrics_pg pg_isready -U postgres -d billable_metrics_db > /dev/null 2>&1
do
  counter=$((counter+1))
  if [ $counter -gt $timeout ]; then
    echo "ERROR: PostgreSQL failed to become ready within ${timeout} seconds"
    docker-compose logs billable_metrics_pg
    exit 1
  fi
  echo "Waiting for PostgreSQL... (${counter}s/${timeout}s)"
  sleep 1
done

echo "PostgreSQL is ready!"

# Wait for application to be healthy
echo "Waiting for application to start..."
timeout=120
counter=0
until curl -f http://localhost:8081/api/health > /dev/null 2>&1
do
  counter=$((counter+1))
  if [ $counter -gt $timeout ]; then
    echo "ERROR: Application failed to start within ${timeout} seconds"
    echo "=== Application Logs ==="
    docker-compose logs billable_metrics_app
    exit 1
  fi
  echo "Waiting for application... (${counter}s/${timeout}s)"
  sleep 2
done

echo ""
echo "================================================"
echo "✅ Deployment successful!"
echo "================================================"
echo "Application: http://localhost:8081"
echo ""
echo "Useful commands:"
echo "  View logs:     docker-compose logs -f"
echo "  View app logs: docker-compose logs -f billable_metrics_app"
echo "  View db logs:  docker-compose logs -f billable_metrics_pg"
echo "  Stop services: docker-compose down"
echo "  Restart:       docker-compose restart"
echo "================================================"
