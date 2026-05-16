#!/bin/bash
# start-services.sh

DEPLOY_PATH=/home/ubuntu/booknest
LOG_PATH=/home/ubuntu/booknest/logs

mkdir -p $LOG_PATH

# ── Stop all running services
echo "Stopping existing services..."
pkill -f "discovery-server" || true
pkill -f "config-server" || true
pkill -f "auth-service" || true
pkill -f "book-service" || true
pkill -f "api-gateway" || true

sleep 3

# ── Start services in order
echo "Starting discovery-server..."
nohup java -jar $DEPLOY_PATH/discovery-server-*.jar \
    > $LOG_PATH/discovery.log 2>&1 &
sleep 15   # wait for eureka to start

echo "Starting config-server..."
nohup java -jar $DEPLOY_PATH/config-server-*.jar \
    > $LOG_PATH/config.log 2>&1 &
sleep 10

echo "Starting auth-service..."
nohup java -jar $DEPLOY_PATH/auth-service-*.jar \
    --DB_USERNAME=root \
    --DB_PASSWORD=yourpassword \
    --JWT_SECRET=yourSecret \
    > $LOG_PATH/auth.log 2>&1 &
sleep 10

echo "Starting book-service..."
nohup java -jar $DEPLOY_PATH/book-service-*.jar \
    --DB_USERNAME=root \
    --DB_PASSWORD=yourpassword \
    > $LOG_PATH/book.log 2>&1 &
sleep 10

echo "Starting api-gateway..."
nohup java -jar $DEPLOY_PATH/api-gateway-*.jar \
    --JWT_SECRET=yourSecret \
    > $LOG_PATH/gateway.log 2>&1 &

echo "✅ All services started!"
echo "Eureka:   http://your-ec2-ip:8761"
echo "Gateway:  http://your-ec2-ip:8080"