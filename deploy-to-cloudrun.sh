#!/bin/bash

# =========================================
# Google Cloud Run Deployment Script
# =========================================
# This script deploys the PromptQuest app to Google Cloud Run with BigQuery

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="promptquest-app"
REGION="us-central1"
DATASET_NAME="promptquest_db"
TABLE_NAME="questions"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  PromptQuest - Cloud Run Deployment${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Step 1: Check if gcloud is installed
echo -e "${YELLOW}Step 1: Checking Google Cloud SDK...${NC}"
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}Error: gcloud CLI is not installed${NC}"
    echo "Install it with: brew install --cask google-cloud-sdk"
    exit 1
fi
echo -e "${GREEN}✓ Google Cloud SDK found${NC}"
echo ""

# Step 2: Get Project ID
echo -e "${YELLOW}Step 2: Getting Project ID...${NC}"
PROJECT_ID=$(gcloud config get-value project 2>/dev/null)
if [ -z "$PROJECT_ID" ]; then
    echo -e "${RED}Error: No project set in gcloud config${NC}"
    echo "Set your project with: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi
echo -e "${GREEN}✓ Using project: ${PROJECT_ID}${NC}"
echo ""

# Step 3: Enable required APIs
echo -e "${YELLOW}Step 3: Enabling required Google Cloud APIs...${NC}"
gcloud services enable run.googleapis.com \
    containerregistry.googleapis.com \
    cloudbuild.googleapis.com \
    bigquery.googleapis.com \
    --project=$PROJECT_ID
echo -e "${GREEN}✓ APIs enabled${NC}"
echo ""

# Step 4: Create BigQuery dataset (skipped - will be auto-created by app)
echo -e "${YELLOW}Step 4: BigQuery dataset setup...${NC}"
echo -e "${GREEN}✓ BigQuery dataset will be auto-created by the application${NC}"
echo ""

# Step 5: Build the application
echo -e "${YELLOW}Step 5: Building application with Maven...${NC}"
mvn clean package -DskipTests
echo -e "${GREEN}✓ Application built successfully${NC}"
echo ""

# Step 6: Build and push Docker image
echo -e "${YELLOW}Step 6: Building and pushing Docker image...${NC}"
IMAGE_NAME="gcr.io/$PROJECT_ID/$APP_NAME:latest"
gcloud builds submit --tag $IMAGE_NAME .
echo -e "${GREEN}✓ Docker image built and pushed${NC}"
echo ""

# Step 7: Deploy to Cloud Run
echo -e "${YELLOW}Step 7: Deploying to Cloud Run...${NC}"
gcloud run deploy $APP_NAME \
    --image $IMAGE_NAME \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --port 8080 \
    --memory 1Gi \
    --cpu 1 \
    --set-env-vars="SPRING_PROFILES_ACTIVE=bigquery,GCP_PROJECT_ID=$PROJECT_ID" \
    --project=$PROJECT_ID

echo -e "${GREEN}✓ Deployment complete!${NC}"
echo ""

# Step 8: Get service URL
echo -e "${YELLOW}Step 8: Getting service URL...${NC}"
SERVICE_URL=$(gcloud run services describe $APP_NAME --platform managed --region $REGION --format 'value(status.url)' --project=$PROJECT_ID)
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Deployment Successful!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Your app is now running at:${NC}"
echo -e "${GREEN}${SERVICE_URL}${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Import questions using the admin panel: ${SERVICE_URL}/admin.html"
echo "2. Test your quiz at: ${SERVICE_URL}"
echo ""
echo -e "${YELLOW}To view logs:${NC}"
echo "  gcloud run services logs read $APP_NAME --region=$REGION"
echo ""
echo -e "${YELLOW}To delete the deployment:${NC}"
echo "  gcloud run services delete $APP_NAME --region=$REGION"
echo ""
