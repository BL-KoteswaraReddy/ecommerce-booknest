# Deploy BookNest Now

Use this exact order.

## 1. AWS Security Group

For your EC2 instance, allow these inbound ports:

```text
22    from your IP only
8080  from your IP only        Jenkins
9090  from 0.0.0.0/0           Backend API gateway
8761  from your IP only        Optional Eureka dashboard
```

Do not use port `8080` for backend. Your Jenkins already owns `8080`.

## 2. Jenkins Credentials

Create these credentials in Jenkins:

```text
dockerhub-creds
ec2-ssh-key
aws-creds
booknest-mysql-password
booknest-rabbitmq-password
booknest-internal-secret
booknest-jwt-secret
google-client-id
google-client-secret
mail-username
mail-password
```

## 3. Backend Jenkins Job

Create Jenkins pipeline job:

```text
Name: booknest-backend
Pipeline script from SCM
Repo: your GitHub repo
Branch: main
Script Path: backend/Jenkinsfile.aws
```

Build with these parameters:

```text
IMAGE_NAMESPACE = your Docker Hub username
IMAGE_TAG = latest
EC2_HOST = ubuntu@3.110.131.226
BACKEND_URL = http://3.110.131.226:9090
FRONTEND_URL = https://d17jaet50zgeez.cloudfront.net
```

After backend job succeeds, SSH into EC2:

```bash
ssh ubuntu@3.110.131.226
cd /home/ubuntu/booknest-backend
docker compose ps
```

You must see:

```text
booknest-api-gateway   ...   0.0.0.0:9090->8080/tcp
```

Test backend:

```bash
curl http://3.110.131.226:9090/api/books
```

If you curl `:8080`, you are testing Jenkins, not backend.

If `curl http://3.110.131.226:9090/api/books` says `Could not connect to server`, check these in order:

```bash
ssh ubuntu@3.110.131.226
cd /home/ubuntu/booknest-backend
ls -a
docker compose ps
docker compose logs --tail=80 api-gateway
```

If the folder does not exist, the backend Jenkins job has not deployed yet.

If `booknest-api-gateway` is not listed, start the backend:

```bash
docker compose up -d
```

If `booknest-api-gateway` is listed but does not show `0.0.0.0:9090->8080/tcp`, redeploy backend with the latest `backend/docker-compose.yml`.

Also confirm the EC2 security group has inbound port `9090` open.

## 4. Frontend Jenkins Job

Create Jenkins pipeline job:

```text
Name: booknest-frontend
Pipeline script from SCM
Repo: your GitHub repo
Branch: main
Script Path: ecommerce-frontend/Jenkinsfile.aws
```

Build with these parameters:

```text
API_URL = http://3.110.131.226:9090
S3_BUCKET = koti-ecommerce-files-2026
CLOUDFRONT_DISTRIBUTION_ID = your CloudFront distribution ID
```

The frontend Jenkinsfile downloads Node.js automatically. If it fails at the deploy stage, install or configure AWS CLI on the Jenkins machine, or make sure the `aws-creds` Jenkins credential exists.

After frontend job succeeds, open:

```text
https://d17jaet50zgeez.cloudfront.net
```

## 5. S3/CloudFront Check

Your S3 bucket root must contain:

```text
index.html
main-xxxxx.js
styles-xxxxx.css
```

If files are inside `browser/`, CloudFront may show `AccessDenied`.

CloudFront settings:

```text
Default root object: index.html
Custom error 403: /index.html, response 200
Custom error 404: /index.html, response 200
```

Then create invalidation:

```text
/*
```

## 6. Google OAuth

In Google Cloud Console add authorized redirect URI:

```text
http://3.110.131.226:9090/login/oauth2/code/google
```

Also make sure frontend origin is allowed in your app:

```text
https://d17jaet50zgeez.cloudfront.net
```
