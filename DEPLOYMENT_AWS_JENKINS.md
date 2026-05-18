# BookNest AWS Jenkins Deployment

## Recommended AWS Split

- Backend: one EC2 Ubuntu server running Docker Compose.
- Frontend: S3 static website files behind CloudFront.
- CI/CD: Jenkins website pipeline jobs using the Jenkinsfiles in this repo.
- Image registry: Docker Hub.

This keeps backend and frontend separate, but both are still in AWS from the user side because the frontend is served by CloudFront/S3.

## Files Added

- `backend/Jenkinsfile.aws`
- `ecommerce-frontend/Jenkinsfile.aws`
- `backend/docker-compose.yml`
- `backend/.env.prod.example`
- Dockerfiles for all backend services that were missing them.

## AWS Setup

### 1. Create EC2 For Backend

Use Ubuntu 22.04 or 24.04.

Recommended size:
- Minimum: `t3.large`
- Better: `t3.xlarge`

This project runs MySQL, Redis, RabbitMQ, Elasticsearch, Eureka, Gateway, and many Spring services. Small instances like `t2.micro` will be painfully slow.

Security group inbound:
- `22` from your IP only
- `9090` from anywhere, or from CloudFront/custom frontend only. Jenkins often uses `8080`, so this guide exposes the API gateway on `9090`.
- `8761` from your IP only, optional for Eureka dashboard

Install Docker on EC2:

```bash
sudo apt update
sudo apt install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker ubuntu
```

Log out and log back in after the last command.

### 2. Create S3 And CloudFront For Frontend

Create an S3 bucket, for example:

```text
booknest-frontend-yourname
```

Recommended:
- Block public access can stay ON if you use CloudFront Origin Access Control.
- Create CloudFront distribution with S3 bucket as origin.
- Set default root object: `index.html`.
- Add custom error responses:
  - 403 -> `/index.html`, HTTP 200
  - 404 -> `/index.html`, HTTP 200

Those error responses are important for Angular routes like `/profile`, `/checkout`, and `/orders/1`.

### 3. Google OAuth Update

In Google Cloud Console, add this authorized redirect URI:

```text
http://YOUR_EC2_PUBLIC_DNS_OR_DOMAIN:9090/login/oauth2/code/google
```

If you later add HTTPS/custom domain, add the HTTPS URL too.

## Jenkins Setup

You do not need Jenkins installed on your laptop. Use your Jenkins website.

Your Jenkins agent must have:
- Docker CLI access
- Maven
- Node.js 20 or 22 recommended
- npm
- AWS CLI

Create Jenkins credentials:

| Credentials ID | Type | Purpose |
|---|---|---|
| `dockerhub-creds` | Username with password | Push Docker images |
| `ec2-ssh-key` | SSH username private key | SSH into EC2 as `ubuntu` |
| `aws-creds` | AWS credentials | Upload frontend to S3 and invalidate CloudFront |
| `booknest-mysql-password` | Secret text | MySQL root password |
| `booknest-rabbitmq-password` | Secret text | RabbitMQ password |
| `booknest-internal-secret` | Secret text | Internal gateway secret |
| `booknest-jwt-secret` | Secret text | JWT signing secret |
| `google-client-id` | Secret text | Google OAuth client id |
| `google-client-secret` | Secret text | Google OAuth client secret |
| `mail-username` | Secret text | Gmail username |
| `mail-password` | Secret text | Gmail app password |

## Backend Pipeline Job

Create a Jenkins Pipeline job:

- Definition: Pipeline script from SCM
- Repository: your GitHub repo
- Branch: `main`
- Script Path: `backend/Jenkinsfile.aws`

Build parameters:

```text
IMAGE_NAMESPACE = your-dockerhub-username
IMAGE_TAG = latest
EC2_HOST = ubuntu@YOUR_EC2_PUBLIC_DNS
BACKEND_URL = http://YOUR_EC2_PUBLIC_DNS:9090
FRONTEND_URL = https://YOUR_CLOUDFRONT_DOMAIN
```

Run the backend job first.

Check backend:

```bash
curl http://YOUR_EC2_PUBLIC_DNS:9090/api/books
```

## Frontend Pipeline Job

Create another Jenkins Pipeline job:

- Definition: Pipeline script from SCM
- Repository: your GitHub repo
- Branch: `main`
- Script Path: `ecommerce-frontend/Jenkinsfile.aws`

Build parameters:

```text
API_URL = http://YOUR_EC2_PUBLIC_DNS:9090
S3_BUCKET = your-s3-bucket-name
CLOUDFRONT_DISTRIBUTION_ID = your-cloudfront-distribution-id
```

Run frontend after backend is healthy.

## Fix CloudFront/S3 `AccessDenied`

If opening the CloudFront URL shows XML like this:

```xml
<Error>
  <Code>AccessDenied</Code>
  <Message>Access Denied</Message>
</Error>
```

check these items:

1. CloudFront default root object must be:

```text
index.html
```

2. The S3 bucket root must contain `index.html` directly. It should look like:

```text
s3://your-bucket/index.html
s3://your-bucket/main-xxxxx.js
s3://your-bucket/styles-xxxxx.css
```

It should not look like:

```text
s3://your-bucket/browser/index.html
s3://your-bucket/booknest-frontend/browser/index.html
```

3. If S3 Block Public Access is ON, CloudFront must use Origin Access Control. Add this bucket policy, replacing the placeholders:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCloudFrontServicePrincipalReadOnly",
      "Effect": "Allow",
      "Principal": {
        "Service": "cloudfront.amazonaws.com"
      },
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::YOUR_BUCKET_NAME/*",
      "Condition": {
        "StringEquals": {
          "AWS:SourceArn": "arn:aws:cloudfront::YOUR_AWS_ACCOUNT_ID:distribution/YOUR_DISTRIBUTION_ID"
        }
      }
    }
  ]
}
```

4. For Angular routing, CloudFront custom error responses must be:

```text
403 -> /index.html -> 200
404 -> /index.html -> 200
```

5. After changing settings or uploading files, invalidate CloudFront:

```bash
aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
```

## Deployment Order

1. Push this repo to GitHub.
2. Run backend Jenkins pipeline.
3. Wait until EC2 containers are healthy.
4. Run frontend Jenkins pipeline.
5. Open CloudFront URL.

## EC2 Useful Commands

```bash
cd /home/ubuntu/booknest-backend
docker compose ps
docker compose logs -f api-gateway
docker compose logs -f auth-service
docker compose logs -f book-service
docker compose restart api-gateway
```

## Important Notes

- Do not use `localhost` in deployed frontend API URLs. Use the EC2 public DNS or your backend domain.
- For serious production, put an Application Load Balancer or Nginx with HTTPS in front of EC2.
- After you get a domain, change:
  - Jenkins backend `BACKEND_URL`
  - Jenkins frontend `API_URL`
  - Google OAuth redirect URI
  - CloudFront custom domain
