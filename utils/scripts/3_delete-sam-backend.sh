#!/bin/bash
set -e

cd "$WORKING_DIR"/functions || {
    echo "Error moving to the 'functions' directory."
    exit 1
}

echo ""
echo "AWS INFORMATION:"
echo ""
echo "- Workloads Profile    : $AWS_WORKLOADS_PROFILE"
echo "- Workloads Region     : $AWS_WORKLOADS_REGION"
echo "- Workloads Environment: $AWS_WORKLOADS_ENV"

echo ""
echo "DELETING SAM PROJECT FROM <WORKLOADS> ACCOUNT..."
sam delete --stack-name "city-data-function"    \
    --config-env "$AWS_WORKLOADS_ENV"           \
    --no-prompts                                \
    --profile "$AWS_WORKLOADS_PROFILE"

### REMOVE SAM CONFIGURATION FILES
rm -rf "$WORKING_DIR"/functions/.aws-sam
