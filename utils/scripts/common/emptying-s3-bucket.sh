#!/bin/bash
set -e

S3_BUCKET_NAME=$1
AWS_PROFILE=$2

### VALIDATING INPUT PARAMETERS
if [ -z "$S3_BUCKET_NAME" ]; then
    echo "ERROR: No bucket name provided."
    exit 1
fi
if [ -z "$AWS_PROFILE" ]; then
    echo "ERROR: No AWS profile provided."
    exit 1
fi

### REMOVE ALL BUCKET VERSION AND DELETE-MARKERS
echo ">> Deleting versions..."
bucket_object_versions=$(dynamodb s3api list-object-versions --bucket "$S3_BUCKET_NAME" --profile "$AWS_PROFILE")
if [[ "$bucket_object_versions" && $(echo "$bucket_object_versions" | jq '.Versions') != "null" ]]; then
    echo "$bucket_object_versions" | jq -r '.Versions[] | "\(.Key) \(.VersionId)"' | while read -r key versionId; do
        dynamodb s3api delete-object                   \
            --bucket "$S3_BUCKET_NAME"            \
            --key "$key"                          \
            --version-id "$versionId"             \
            --profile "$AWS_PROFILE" >/dev/null
  done
fi

### EMPTY S3_BUCKET_NAME
echo ">> Emptying..."
dynamodb s3 rm "s3://$S3_BUCKET_NAME"                  \
    --recursive                                   \
    --profile "$AWS_PROFILE" >/dev/null
