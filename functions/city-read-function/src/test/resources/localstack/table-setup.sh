#!/bin/bash

echo ""
echo "CREATING DYNAMODB TABLE..."
awslocal dynamodb create-table                  \
    --table-name 'City'                         \
    --attribute-definitions                     \
      AttributeName='pk',AttributeType=S        \
      AttributeName='sk',AttributeType=S        \
    --key-schema                                \
      AttributeName='pk',KeyType=HASH           \
      AttributeName='sk',KeyType=RANGE          \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

echo ""
echo "WRITING ITEMS INTO DYNAMODB..."
awslocal dynamodb batch-write-item  \
  --request-items file:///var/lib/localstack/table-data.json
