#!/bin/bash

echo ""
echo "WAITING FOR CITY DATA RESOURCES FROM BUILDER CONTAINER..."
DATA_FUNCTION_PATH="/var/tmp/city-data/data-function-assembly.zip"
while [ ! -f "$DATA_FUNCTION_PATH" ]; do
  sleep 1
done
echo "DONE!"

echo ""
echo "CREATING LAMBDA ROLE..."
awslocal iam create-role                    \
    --role-name 'lambda-role'               \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Principal": {
              "Service": "lambda.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
          }
        ]
      }'

echo ""
echo "ALLOWING LAMBDA TO ACCESS LOGS..."
awslocal iam put-role-policy                \
    --role-name 'lambda-role'               \
    --policy-name 'CloudWatchLogsPolicy'    \
    --policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents"
            ],
            "Resource": "arn:aws:logs:*:*:*"
          }
        ]
      }'

echo ""
echo "ALLOWING LAMBDA TO ACCESS DYNAMODB..."
awslocal iam put-role-policy                \
    --role-name 'lambda-role'               \
    --policy-name 'DynamoDBPolicy'          \
    --policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "dynamodb:GetItem",
              "dynamodb:Scan",
              "dynamodb:Query"
            ],
            "Resource": "arn:aws:dynamodb:us-east-1:000000000000:table/Cities"
          }
        ]
      }'

echo ""
echo "CREATING CITY DATA FUNCTION..."
awslocal lambda create-function                                                               \
  --function-name 'city-data-function'                                                        \
  --runtime 'provided.al2023'                                                                 \
  --architectures 'arm64'                                                                     \
  --zip-file fileb://"$DATA_FUNCTION_PATH"                                                    \
  --handler 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest'   \
  --timeout 20                                                                                \
  --memory-size 512                                                                           \
  --role 'arn:aws:iam::000000000000:role/lambda-role'                                         \
  --environment 'Variables={SPRING_CLOUD_AWS_ENDPOINT=http://host.docker.internal:4566}'

echo ""
echo "DONE!"
