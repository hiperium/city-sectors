#!/bin/bash
set -e

cd "$WORKING_DIR"/functions || {
    echo "Error moving to the 'functions' directory."
    exit 1
}

echo ""
echo "AWS INFORMATION:"
echo ""
echo "- Workloads Environment: $AWS_WORKLOADS_ENV"
echo "- Workloads Profile    : $AWS_WORKLOADS_PROFILE"
echo "- Workloads Account    : $AWS_WORKLOADS_ACCOUNT_ID"
echo "- Workloads Region     : $AWS_WORKLOADS_REGION"

echo ""
echo "VALIDATING SAM TEMPLATE..."
sam validate --lint

echo ""
echo "BUILDING SAM PROJECT LOCALLY..."
unbuffer sam build

echo ""
echo "DEPLOYING SAM PROJECT INTO AWS..."
sam deploy                                                    \
    --parameter-overrides SpringProfile="$AWS_WORKLOADS_ENV"  \
    --no-confirm-changeset                                    \
    --disable-rollback                                        \
    --profile "$AWS_WORKLOADS_PROFILE"

### LOADING TEST DATA INTO DYNAMODB ONLY IN DEV ENVIRONMENT
if [ "$AWS_WORKLOADS_ENV" == "dev" ]; then
    echo ""
    echo "LOADING DATA INTO DYNAMODB..."
    aws dynamodb batch-write-item \
      --request-items file://"$WORKING_DIR"/functions/city-data-function/src/test/resources/localstack/table-data.json \
      --profile "$AWS_WORKLOADS_PROFILE" > /dev/null
fi
