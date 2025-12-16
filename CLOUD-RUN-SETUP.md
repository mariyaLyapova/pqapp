# Google Cloud Run + BigQuery Setup Guide

This guide will help you deploy your PromptQuest application to Google Cloud Run with BigQuery as the database.

## 📋 Prerequisites

Before you begin, make sure you have:

1. **Google Cloud Account** - [Sign up here](https://cloud.google.com/)
2. **Google Cloud Project** - Create one in the [GCP Console](https://console.cloud.google.com/)
3. **Billing Enabled** - Cloud Run requires billing to be enabled
4. **Google Cloud SDK** - Install it on your machine

### Install Google Cloud SDK

```bash
# macOS
brew install --cask google-cloud-sdk

# Or download from: https://cloud.google.com/sdk/docs/install
```

## 🚀 Quick Start (Automated Deployment)

The easiest way to deploy is using the automated script:

### Step 1: Authenticate with Google Cloud

```bash
gcloud auth login
```

### Step 2: Set Your Project

```bash
gcloud config set project YOUR_PROJECT_ID
```

### Step 3: Run the Deployment Script

```bash
./deploy-to-cloudrun.sh
```

That's it! The script will:
- ✅ Enable required Google Cloud APIs
- ✅ Create BigQuery dataset
- ✅ Build your application
- ✅ Build and push Docker image
- ✅ Deploy to Cloud Run
- ✅ Give you the URL of your live app

---

## 📖 Manual Step-by-Step Setup

If you prefer to understand each step or customize the deployment:

### Step 1: Install Prerequisites

```bash
# Install gcloud CLI (if not already installed)
brew install --cask google-cloud-sdk

# Verify installation
gcloud --version
```

### Step 2: Authenticate and Configure

```bash
# Login to Google Cloud
gcloud auth login

# List your projects
gcloud projects list

# Set your project (replace with your project ID)
gcloud config set project YOUR_PROJECT_ID

# Configure Docker to use gcloud
gcloud auth configure-docker
```

### Step 3: Enable Required APIs

```bash
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable cloudbuild.googleapis.com
gcloud services enable bigquery.googleapis.com
```

### Step 4: Create BigQuery Dataset

```bash
# Create the dataset
bq mk --dataset --location=us-central1 YOUR_PROJECT_ID:promptquest_db

# Verify it was created
bq ls
```

### Step 5: Build Your Application

```bash
# Build the JAR file
mvn clean package -DskipTests
```

### Step 6: Build and Push Docker Image

```bash
# Build and submit to Google Cloud Build
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/promptquest-app:latest .
```

### Step 7: Deploy to Cloud Run

```bash
gcloud run deploy promptquest-app \
  --image gcr.io/YOUR_PROJECT_ID/promptquest-app:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 1Gi \
  --cpu 1 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=bigquery,GCP_PROJECT_ID=YOUR_PROJECT_ID"
```

### Step 8: Get Your App URL

```bash
gcloud run services describe promptquest-app \
  --platform managed \
  --region us-central1 \
  --format 'value(status.url)'
```

---

## 🔧 Configuration Details

### Application Profiles

The app supports two profiles:

1. **Default (SQLite)** - For local development
   ```bash
   mvn spring-boot:run
   ```

2. **BigQuery** - For Cloud Run deployment
   ```bash
   SPRING_PROFILES_ACTIVE=bigquery GCP_PROJECT_ID=your-project mvn spring-boot:run
   ```

### Environment Variables

When deploying to Cloud Run, these environment variables are set:

- `SPRING_PROFILES_ACTIVE=bigquery` - Activates BigQuery profile
- `GCP_PROJECT_ID=YOUR_PROJECT_ID` - Your Google Cloud project ID

### BigQuery Configuration

The BigQuery setup is defined in `src/main/resources/application-bigquery.properties`:

```properties
gcp.project.id=${GCP_PROJECT_ID}
bigquery.dataset=promptquest_db
bigquery.table=questions
```

---

## 📊 Using BigQuery

### View Your Data

```bash
# List tables in your dataset
bq ls promptquest_db

# Query questions
bq query --use_legacy_sql=false \
  'SELECT * FROM `YOUR_PROJECT_ID.promptquest_db.questions` LIMIT 10'

# Count questions
bq query --use_legacy_sql=false \
  'SELECT COUNT(*) FROM `YOUR_PROJECT_ID.promptquest_db.questions`'
```

### Import Questions

1. Go to your app's admin panel: `https://YOUR_APP_URL/admin.html`
2. Upload your JSON file with questions
3. Questions will be automatically stored in BigQuery

---

## 🔍 Monitoring & Debugging

### View Logs

```bash
# Real-time logs
gcloud run services logs tail promptquest-app --region=us-central1

# Recent logs
gcloud run services logs read promptquest-app --region=us-central1 --limit=50
```

### Check Service Status

```bash
gcloud run services describe promptquest-app \
  --platform managed \
  --region us-central1
```

### Test API Endpoints

```bash
# Get all questions
curl https://YOUR_APP_URL/api/quiz/questions

# Get random questions
curl https://YOUR_APP_URL/api/quiz/random/5
```

---

## 💰 Cost Considerations

Cloud Run pricing is based on:
- **CPU and Memory** - Only charged when handling requests
- **Requests** - First 2 million requests/month are free
- **BigQuery** - First 1TB of queries/month is free

**Estimated costs for small apps**: $0 - $5/month (likely free tier)

---

## 🔐 Security Best Practices

### 1. Restrict Public Access (Optional)

If you want to restrict access:

```bash
gcloud run services update promptquest-app \
  --no-allow-unauthenticated \
  --region=us-central1
```

### 2. Use IAM for BigQuery

The Cloud Run service automatically uses its service account to access BigQuery. No credentials needed!

### 3. Enable HTTPS

Cloud Run automatically provides HTTPS endpoints. Always use the `https://` URL.

---

## 🔄 Updating Your Deployment

To update your app after making changes:

```bash
# Option 1: Use the deployment script
./deploy-to-cloudrun.sh

# Option 2: Manual update
mvn clean package -DskipTests
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/promptquest-app:latest .
gcloud run deploy promptquest-app \
  --image gcr.io/YOUR_PROJECT_ID/promptquest-app:latest \
  --region=us-central1
```

---

## 🗑️ Cleanup

To delete resources and stop charges:

```bash
# Delete Cloud Run service
gcloud run services delete promptquest-app --region=us-central1

# Delete Docker images
gcloud container images delete gcr.io/YOUR_PROJECT_ID/promptquest-app:latest

# Delete BigQuery dataset (WARNING: This deletes all data!)
bq rm -r -f -d promptquest_db
```

---

## 🐛 Troubleshooting

### Issue: "Permission denied" errors

**Solution**: Make sure billing is enabled and APIs are activated:
```bash
gcloud services enable run.googleapis.com bigquery.googleapis.com
```

### Issue: Application won't start

**Solution**: Check logs for errors:
```bash
gcloud run services logs tail promptquest-app --region=us-central1
```

### Issue: Can't connect to BigQuery

**Solution**: Verify environment variables are set:
```bash
gcloud run services describe promptquest-app \
  --region=us-central1 \
  --format='value(spec.template.spec.containers[0].env)'
```

### Issue: "gcloud: command not found"

**Solution**: Install Google Cloud SDK:
```bash
brew install --cask google-cloud-sdk
```

Then restart your terminal or run:
```bash
source ~/.zshrc
```

---

## 📚 Additional Resources

- [Google Cloud Run Documentation](https://cloud.google.com/run/docs)
- [BigQuery Documentation](https://cloud.google.com/bigquery/docs)
- [Spring Boot on Cloud Run](https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-java-service)
- [Cloud Run Pricing](https://cloud.google.com/run/pricing)

---

## ✅ Success Checklist

After deployment, verify:

- [ ] App is accessible at the Cloud Run URL
- [ ] Admin panel loads: `/admin.html`
- [ ] Quiz page loads: `/index.html`
- [ ] You can import questions via admin panel
- [ ] Questions are stored in BigQuery
- [ ] Quiz functionality works correctly

---

## 🎉 You're Done!

Your PromptQuest app is now running on Google Cloud Run with BigQuery!

**Your app URL**: Check the output of the deployment script or run:
```bash
gcloud run services describe promptquest-app --region=us-central1 --format='value(status.url)'
```
