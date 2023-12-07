#!/bin/bash

# Check if BRANCH_TO_DEPLOY_FROM is not empty
if [ -z "$BRANCH_TO_DEPLOY_FROM" ]; then
    echo "Error: BRANCH_TO_DEPLOY_FROM is empty"
    exit 1
fi

# Check if DEPLOYER_NAME is not empty
if [ -z "$DEPLOYER_NAME" ]; then
    echo "Error: DEPLOYER_NAME is empty"
    exit 1
fi

# Your deployment script logic goes here
echo "Starting deployment from branch '$BRANCH_TO_DEPLOY_FROM' by '$DEPLOYER_NAME'"

# Rest of the script...