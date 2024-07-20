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

### REMOVING SAM LOCAL ARTIFACTS
rm -rf "$WORKING_DIR"/functions/.aws-sam

echo ""
echo "VALIDATING SAM TEMPLATE..."
sam validate --lint
echo "DONE!"

echo ""
echo "BUILDING SAM PROJECT LOCALLY..."
unbuffer sam build --config-env "$AWS_WORKLOADS_ENV"
echo ""
echo "DONE!"

echo ""
echo "DEPLOYING SAM PROJECT INTO AWS..."
sam deploy                                                    \
    --config-env "$AWS_WORKLOADS_ENV"                         \
    --parameter-overrides SpringProfile="$AWS_WORKLOADS_ENV"  \
    --no-confirm-changeset                                    \
    --disable-rollback                                        \
    --profile "$AWS_WORKLOADS_PROFILE"
echo ""
echo "DONE!"

echo ""
echo "LOADING DATA INTO DYNAMODB..."
aws dynamodb batch-write-item \
  --request-items file://"$WORKING_DIR"/functions/city-data-function/src/test/resources/localstack/table-data.json \
  --profile "$AWS_WORKLOADS_PROFILE" > /dev/null
