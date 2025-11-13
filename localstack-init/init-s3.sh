#!/bin/bash
echo "Creating S3 bucket..."
awslocal s3 mb s3://images
echo "LocalStack S3 initialization completed"
