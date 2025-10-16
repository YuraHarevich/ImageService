#!/bin/bash
echo "Starting S3 initialization..."
until aws --endpoint-url=http://localhost:4566 s3 ls; do
  echo "Waiting for LocalStack S3..."
  sleep 2
done

BUCKET_NAME="images-bucket"
if aws --endpoint-url=http://localhost:4566 s3 ls "s3://$BUCKET_NAME" 2>/dev/null; then
    echo "Bucket $BUCKET_NAME already exists. Skipping initialization."
    exit 0
fi

echo "Creating bucket $BUCKET_NAME..."
aws --endpoint-url=http://localhost:4566 s3 mb s3://$BUCKET_NAME
aws --endpoint-url=http://localhost:4566 s3api put-object --bucket $BUCKET_NAME --key icons/

ICONS_DIR="/etc/localstack/icons"

for icon_file in $ICONS_DIR/*.svg; do
    if [ -f "$icon_file" ]; then
        icon_name=$(basename "$icon_file")
        echo "Uploading icon: $icon_name"

        aws --endpoint-url=http://localhost:4566 s3api put-object \
            --bucket $BUCKET_NAME \
            --key "icons/$icon_name" \
            --body "$icon_file" \
            --content-type "image/svg+xml"
    fi
done

echo "Uploaded icons:"
aws --endpoint-url=http://localhost:4566 s3 ls s3://$BUCKET_NAME/icons/ --recursive
echo "S3 initialization completed!"