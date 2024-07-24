#!/bin/bash
set -e

WORKING_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
export WORKING_DIR

function setEnvironmentVariables() {
    echo ""
    read -r -p 'Enter the <Environment> to deploy the Hiperium service: [dev] ' env_name
    echo ""
    echo "Wait a moment please..."

    if [ -z "$env_name" ]; then
        AWS_WORKLOADS_ENV='dev'
    else
        AWS_WORKLOADS_ENV=$(echo "$env_name" | tr '[:upper:]' '[:lower:]')
    fi
    export AWS_WORKLOADS_ENV

    ### WORKLOADS PROFILE
    AWS_WORKLOADS_PROFILE="city-$AWS_WORKLOADS_ENV"
    export AWS_WORKLOADS_PROFILE
    "$WORKING_DIR"/utils/scripts/common/verify-aws-profile-existence.sh "$AWS_WORKLOADS_PROFILE"

    ### AWS ACCOUNTS
    AWS_WORKLOADS_ACCOUNT_ID=$(aws configure get sso_account_id --profile "$AWS_WORKLOADS_PROFILE")
    export AWS_WORKLOADS_ACCOUNT_ID

    ### AWS REGIONS
    AWS_WORKLOADS_REGION=$(aws configure get region --profile "$AWS_WORKLOADS_PROFILE")
    export AWS_WORKLOADS_REGION
}

helperMenu() {
    echo "
    *********************************************
    **************** Helper Menu ****************
    *********************************************
     b) Prune Docker System.
     c) Remaining Session Time.
     d) Print Environment Variables.
    ---------------------------------------------
     r) Return.
     q) Quit.

    "
    read -r -p 'Choose an option: ' option
    case $option in
    [Bb])
        clear
        "$WORKING_DIR"/utils/scripts/common/docker-system-prune.sh
        clear
        echo ""
        echo "DONE!"
        helperMenu
        ;;
    [Cc])
        clear
        printRemainingSessionTime
        echo ""
        read -r -p 'Press [Enter] key to continue...'
        clear
        echo "DONE!"
        helperMenu
        ;;
    [Dd])
        clear
        echo
        "$WORKING_DIR"/utils/scripts/common/print-global-variables.sh
        clear
        echo "DONE!"
        helperMenu
        ;;
    [Rr])
        clear
        menu
        ;;
    [Qq])
        clear
        echo ""
        echo "DONE!"
        echo ""
        exit 0
        ;;
    *)
        clear
        echo 'Wrong option.'
        helperMenu
        ;;
    esac
}

function printRemainingSessionTime() {
  clear
  "$WORKING_DIR"/utils/scripts/common/verify-remaining-session-time.sh "true"
}

menu() {
    echo "
    *******************************************
    **************** Main Menu ****************
    *******************************************
     1) Docker Compose.
     2) Create Backend.
     3) Delete Backend.
    -------------------------------------------
     h) Helper Menu.
     q) Quit.

    "
    read -r -p 'Choose an option: ' option
    case $option in
    [Hh])
        clear
        helperMenu
        ;;
    1)
        "$WORKING_DIR"/utils/scripts/1_deploy-docker-cluster.sh
        ;;
    2)
        printRemainingSessionTime
        "$WORKING_DIR"/utils/scripts/2_create-sam-backend.sh
        clear
        echo ""
        echo "DONE!"
        menu
        ;;
    3)
        printRemainingSessionTime
        "$WORKING_DIR"/utils/scripts/3_delete-sam-backend.sh
        clear
        echo ""
        echo "DONE!"
        menu
        ;;
    [Qq])
        clear
        echo ""
        echo "DONE!"
        exit 0
        ;;
    *)
        clear
        echo 'Wrong option.'
        menu
        ;;
    esac
}

#### Main function ####
clear
setEnvironmentVariables
clear
menu
