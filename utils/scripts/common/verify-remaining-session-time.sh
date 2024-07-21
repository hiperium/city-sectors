#!/bin/bash
set -e

SHOW_TIME_REMAINING=$1

if [ -z "$AWS_WORKLOADS_PROFILE" ]; then
    echo ""
    echo "ERROR: AWS workloads profile is required."
    echo ""
    return 1
fi

### CHECK IF THE VERIFICATION IS ONLY FOR THE EXPIRATION DATE
if [[ -z "$SHOW_TIME_REMAINING" ]]; then
    SHOW_TIME_REMAINING="false"
fi

### GETTING START-URL FROM THE AWS PROFILE
SSO_START_URL=$(grep -A 2 "\[sso-session city-sso\]" ~/.aws/config | grep "sso_start_url" | awk -F' = ' '{print $2}')
if [[ -z "$SSO_START_URL" ]]; then
    echo "ERROR: SSO start URL not found for AWS profile '$AWS_WORKLOADS_PROFILE'." >&2
    exit 1
fi

### CHECK IF THE SSO CACHE DIRECTORY EXISTS
SSO_CACHE_DIR="${HOME}/.aws/sso/cache"
if [[ ! -d "$SSO_CACHE_DIR" ]]; then
    echo "ERROR: SSO cache directory not found: '$SSO_CACHE_DIR'" >&2
    exit 1
fi

#FIND THE SSO SESSION FILE FOR THE START-URL
SSO_CACHE_FILE=$(grep -l "\"startUrl\": \"$SSO_START_URL\"" "$SSO_CACHE_DIR"/*.json | tail -n1)

### CHECK IF THE SSO SESSION FILE WAS FOUND
if [[ -z "$SSO_CACHE_FILE" ]]; then
    echo "ERROR: SSO session file not found for AWS profile '$AWS_WORKLOADS_PROFILE'." >&2
    exit 1
fi

### GET THE EXPIRATION DATE FROM THE SSO SESSION FILE
EXPIRATION_DATE=$(jq -r '.expiresAt' "$SSO_CACHE_FILE")

### CHECK IF THE EXPIRATION DATE WAS FOUND
if [[ -z "$EXPIRATION_DATE" ]]; then
    echo "ERROR: SSO expiration date not found for AWS profile '$AWS_WORKLOADS_PROFILE'." >&2
    exit 1
fi

### CALCULATE THE TIME REMAINING IN MINUTES
EXPIRATION_EPOCH=$(date -j -f "%Y-%m-%dT%H:%M:%SZ" "$EXPIRATION_DATE" +"%s")
CURRENT_EPOCH=$(date +"%s")
DIFF_MINUTES=$(( (EXPIRATION_EPOCH - CURRENT_EPOCH) / 60 ))
if [[ $DIFF_MINUTES -le 0 ]]; then
    echo ""
    echo "ERROR: SSO session for AWS profile '$AWS_WORKLOADS_PROFILE' has expired." >&2
    echo ""
    exit 1
fi

### CHECK IF THE VERIFICATION IS ONLY FOR THE EXPIRATION DATE
if [[ "$SHOW_TIME_REMAINING" == "true" ]]; then
    echo ""
    echo "The <Expiration Date> of your session is: $EXPIRATION_DATE"
    HOURS=$(( DIFF_MINUTES / 60 ))
    MINUTES=$(( DIFF_MINUTES % 60 ))
    if [[ $HOURS -gt 0 ]]; then
        echo "Time remaining: $HOURS hours and $MINUTES minutes."
    else
        echo "Time remaining: $DIFF_MINUTES minutes."
    fi
    echo ""
fi
