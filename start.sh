#!/bin/bash

# Billable Metrics Service - Easy Start Script
# This script builds and starts the application with Docker Compose

echo "🚀 Starting Billable Metrics Service..."
echo ""

# Step 1: Build the application
echo "📦 Building application JAR..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

# Step 2: Copy JAR to root
echo "📋 Copying JAR file..."
cp target/app.jar .

# Step 3: Stop any running containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Step 4: Start containers with fresh build
echo "🐳 Starting Docker containers..."
docker-compose up -d --build

# Step 5: Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 10

# Step 6: Check status
echo ""
echo "📊 Container Status:"
docker ps --filter name=billable_metrics

echo ""
echo "✅ Done! Application should be running on http://localhost:8081"
echo "📝 Check logs with: docker logs billable_metrics_app"
