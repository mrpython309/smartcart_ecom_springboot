# ===================================================================
# SmartCart - AWS App Runner & S3 Automated Deployment Script
# ===================================================================
# Requirements:
# 1. AWS CLI installed and configured ('aws configure')
# 2. Docker Desktop running on your machine
# 3. Node.js & npm installed locally
# ===================================================================

# Automatically inject default AWS CLI v2 installation path if not already in PATH
if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Amazon\AWSCLIV2\aws.exe") {
        $env:Path = "C:\Program Files\Amazon\AWSCLIV2;" + $env:Path
    }
}

$ErrorActionPreference = "Stop"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " Starting SmartCart AWS Deployment..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# -------------------------------------------------------------------
# 1. Check AWS CLI Authentication and Identity
# -------------------------------------------------------------------
Write-Host "Checking AWS CLI authentication..." -ForegroundColor Yellow
try {
    $identity = aws sts get-caller-identity --query "[Account, Arn]" --output json | ConvertFrom-Json
    $ACCOUNT_ID = $identity[0]
    $REGION = aws configure get region
    if ([string]::IsNullOrEmpty($REGION)) {
        $REGION = "us-east-1"
    }
    Write-Host "Authenticated with AWS Account: $ACCOUNT_ID in Region: $REGION" -ForegroundColor Green
}
catch {
    Write-Host "Failed to authenticate with AWS CLI. Please run 'aws configure' first." -ForegroundColor Red
    exit 1
}

# -------------------------------------------------------------------
# 2. Verify Docker is Running
# -------------------------------------------------------------------
Write-Host "Verifying Docker is running..." -ForegroundColor Yellow
try {
    docker info > $null
    Write-Host "Docker is active and running." -ForegroundColor Green
}
catch {
    Write-Host "Docker is not running. Please start Docker Desktop and run the script again." -ForegroundColor Red
    exit 1
}

# -------------------------------------------------------------------
# 3. Setup Unique Resource Names
# -------------------------------------------------------------------
$suffix = $ACCOUNT_ID.Substring(0, 6)
$UPLOAD_BUCKET = "smartcart-uploads-$suffix"
$FRONTEND_BUCKET = "smartcart-shop-$suffix"
Write-Host "Using Bucket Names:" -ForegroundColor Cyan
Write-Host "  - Media Uploads: $UPLOAD_BUCKET" -ForegroundColor Cyan
Write-Host "  - Frontend Host: $FRONTEND_BUCKET" -ForegroundColor Cyan

# -------------------------------------------------------------------
# 4. Create S3 Uploads Bucket
# -------------------------------------------------------------------
Write-Host "Creating S3 bucket for media uploads..." -ForegroundColor Yellow
try {
    if ($REGION -eq "us-east-1") {
        aws s3api create-bucket --bucket $UPLOAD_BUCKET --region $REGION > $null
    }
    else {
        aws s3api create-bucket --bucket $UPLOAD_BUCKET --region $REGION --create-bucket-configuration LocationConstraint=$REGION > $null
    }
    # Disable Block Public Access for upload assets (so they can be read by clients)
    aws s3api put-public-access-block --bucket $UPLOAD_BUCKET --public-access-block-configuration "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
    
    # Apply S3 bucket policy to allow read access to uploaded images
    $policy = @"
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::$UPLOAD_BUCKET/uploads/*"
        }
    ]
}
"@
    $policy | Out-File -FilePath s3-policy.json -Encoding utf8
    aws s3api put-bucket-policy --bucket $UPLOAD_BUCKET --policy file://s3-policy.json
    Remove-Item s3-policy.json
    Write-Host "S3 upload bucket created and public-read access configured." -ForegroundColor Green
}
catch {
    Write-Host "Warning/Error during S3 upload bucket setup: $_. Continuing..." -ForegroundColor Yellow
}

# -------------------------------------------------------------------
# 5. Create Amazon RDS MySQL Database
# -------------------------------------------------------------------
Write-Host "Setting up Amazon RDS MySQL Database..." -ForegroundColor Yellow
$dbPassword = Read-Host -Prompt "Enter a SECURE master password for RDS database 'smartcart' (min 8 chars)"
if ($dbPassword.Length -lt 8) {
    Write-Host "Password must be at least 8 characters long." -ForegroundColor Red
    exit 1
}

Write-Host "Launching RDS MySQL Instance 'smartcart-db'... This takes 5-10 minutes." -ForegroundColor Yellow
try {
    aws rds create-db-instance `
        --db-instance-identifier smartcart-db `
        --db-instance-class db.t3.micro `
        --engine mysql `
        --master-username smartcart `
        --master-user-password $dbPassword `
        --allocated-storage 20 `
        --db-name smartcart_db `
        --publicly-accessible `
        --no-cli-pager > $null
}
catch {
    Write-Host "RDS Database might already exist or is launching. Continuing..." -ForegroundColor Yellow
}

# -------------------------------------------------------------------
# 6. Build and Push Backend Docker Image to ECR
# -------------------------------------------------------------------
Write-Host "Creating Amazon ECR Repository..." -ForegroundColor Yellow
try {
    aws ecr create-repository --repository-name smartcart-backend --no-cli-pager > $null
}
catch {
    Write-Host "ECR Repository already exists. Continuing..." -ForegroundColor Yellow
}

Write-Host "Logging Docker into Amazon ECR..." -ForegroundColor Yellow
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

Write-Host "Building Java 25 production backend Docker container..." -ForegroundColor Yellow
docker build -t smartcart-backend ./smartcart-backend

Write-Host "Tagging and pushing container image to ECR..." -ForegroundColor Yellow
docker tag smartcart-backend:latest "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/smartcart-backend:latest"
docker push "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/smartcart-backend:latest"
Write-Host "Docker image pushed to ECR successfully." -ForegroundColor Green

# -------------------------------------------------------------------
# 7. Create IAM App Runner ECR Access Role
# -------------------------------------------------------------------
Write-Host "Setting up App Runner IAM roles..." -ForegroundColor Yellow
$trustPolicy = '{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "build.apprunner.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}'
$trustPolicy | Out-File -FilePath trust-policy.json -Encoding utf8
try {
    aws iam create-role --role-name AppRunnerECRAccessRole --assume-role-policy-document file://trust-policy.json --no-cli-pager > $null
    aws iam attach-role-policy --role-name AppRunnerECRAccessRole --policy-arn arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRDataAccess
}
catch {
    Write-Host "AppRunnerECRAccessRole role already exists. Continuing..." -ForegroundColor Yellow
}
Remove-Item trust-policy.json
Start-Sleep -Seconds 5 # Wait for IAM role replication

# -------------------------------------------------------------------
# 8. Wait for RDS Database Availability
# -------------------------------------------------------------------
Write-Host "Waiting for RDS database to start up completely..." -ForegroundColor Yellow
while ($true) {
    $status = aws rds describe-db-instances --db-instance-identifier smartcart-db --query "DBInstances[0].DBInstanceStatus" --output text
    Write-Host "Current Database Status: $status" -ForegroundColor Gray
    if ($status -eq "available") {
        break
    }
    Start-Sleep -Seconds 15
}
$DB_ENDPOINT = aws rds describe-db-instances --db-instance-identifier smartcart-db --query "DBInstances[0].Endpoint.Address" --output text
Write-Host "Database is online at endpoint: $DB_ENDPOINT" -ForegroundColor Green

# -------------------------------------------------------------------
# 9. Deploy Backend to AWS App Runner
# -------------------------------------------------------------------
Write-Host "Deploying Backend to AWS App Runner..." -ForegroundColor Yellow
try {
    aws apprunner create-service `
        --service-name smartcart-backend-service `
        --source-configuration "ImageRepository={ImageIdentifier=$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/smartcart-backend:latest,ImageRepositoryType=ECR,ImageConfiguration={Port=8080,RuntimeEnvironmentVariables=[{Name=SPRING_PROFILES_ACTIVE,Value=prod},{Name=SPRING_DATASOURCE_URL,Value=jdbc:mysql://$DB_ENDPOINT:3306/smartcart_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true},{Name=SPRING_DATASOURCE_USERNAME,Value=smartcart},{Name=SPRING_DATASOURCE_PASSWORD,Value=$dbPassword},{Name=AWS_S3_BUCKET,Value=$UPLOAD_BUCKET},{Name=AWS_REGION,Value=$REGION}]}},AuthenticationConfiguration={AccessRoleArn=arn:aws:iam::$ACCOUNT_ID:role/AppRunnerECRAccessRole}" `
        --no-cli-pager > $null
}
catch {
    Write-Host "App Runner service is already created or updating. Continuing..." -ForegroundColor Yellow
}

Write-Host "Waiting for App Runner Service deployment..." -ForegroundColor Yellow
while ($true) {
    $service = aws apprunner list-services --query "ServiceSummaryList[?ServiceName=='smartcart-backend-service'][0]" --output json | ConvertFrom-Json
    $status = $service.Status
    Write-Host "Current App Runner Status: $status" -ForegroundColor Gray
    if ($status -eq "RUNNING") {
        $BACKEND_URL = $service.ServiceUrl
        break
    }
    Start-Sleep -Seconds 15
}
Write-Host "Backend is online at: https://$BACKEND_URL" -ForegroundColor Green

# -------------------------------------------------------------------
# 10. Build and Deploy React Frontend to S3 Static Website Host
# -------------------------------------------------------------------
Write-Host "Building React Frontend..." -ForegroundColor Yellow
$env:VITE_API_BASE_URL = "https://$BACKEND_URL/api"
Write-Host "Configuring React API Base URL to: https://$BACKEND_URL/api" -ForegroundColor Gray

# Build static bundle
Set-Location smartcart-frontend
npm ci
npm run build
Set-Location ..

Write-Host "Creating S3 bucket for Frontend website hosting..." -ForegroundColor Yellow
try {
    if ($REGION -eq "us-east-1") {
        aws s3api create-bucket --bucket $FRONTEND_BUCKET --region $REGION > $null
    }
    else {
        aws s3api create-bucket --bucket $FRONTEND_BUCKET --region $REGION --create-bucket-configuration LocationConstraint=$REGION > $null
    }
    
    # Configure public access
    aws s3api put-public-access-block --bucket $FRONTEND_BUCKET --public-access-block-configuration "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
    
    # Policy for static hosting
    $frontPolicy = @"
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::$FRONTEND_BUCKET/*"
        }
    ]
}
"@
    $frontPolicy | Out-File -FilePath s3-front-policy.json -Encoding utf8
    aws s3api put-bucket-policy --bucket $FRONTEND_BUCKET --policy file://s3-front-policy.json
    Remove-Item s3-front-policy.json

    # Set as S3 static website hosting
    aws s3 website "s3://$FRONTEND_BUCKET/" --index-document index.html --error-document index.html
    Write-Host "S3 static website hosting configured." -ForegroundColor Green
}
catch {
    Write-Host "Warning/Error during S3 frontend setup: $_. Continuing..." -ForegroundColor Yellow
}

Write-Host "Uploading React distribution to S3..." -ForegroundColor Yellow
aws s3 sync ./smartcart-frontend/dist/ "s3://$FRONTEND_BUCKET/"

$WEBSITE_URL = "http://$FRONTEND_BUCKET.s3-website-$REGION.amazonaws.com"
if ($REGION -eq "us-east-1") {
    $WEBSITE_URL = "http://$FRONTEND_BUCKET.s3-website.amazonaws.com"
}

Write-Host "========================================================" -ForegroundColor Green
Write-Host " Deployment Completed Successfully! " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host "Frontend Static URL: $WEBSITE_URL" -ForegroundColor Green
Write-Host "Backend API URL:     https://$BACKEND_URL" -ForegroundColor Green
Write-Host "Database Endpoint:   $DB_ENDPOINT" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
