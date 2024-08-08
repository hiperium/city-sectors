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
echo "DELETING SAM PROJECT FROM <WORKLOADS> ACCOUNT..."
sam delete        \
  --no-prompts    \
  --profile "$AWS_WORKLOADS_PROFILE"

echo ""
echo "DELETING EXISTING LOG-GROUPS FROM CLOUDWATCH..."
aws logs describe-log-groups --output text \
    --query "logGroups[?contains(logGroupName, 'cities-sam-cli')].[logGroupName]" \
    --profile "$AWS_WORKLOADS_PROFILE" | while read -r log_group_name; do
        echo "- Deleting log-group: $log_group_name"
        aws logs delete-log-group                 \
            --log-group-name "$log_group_name"    \
            --profile "$AWS_WORKLOADS_PROFILE"
done
echo "DONE!"

### REMOVE SAM CONFIGURATION FILES
rm -rf "$WORKING_DIR"/functions/.aws-sam
