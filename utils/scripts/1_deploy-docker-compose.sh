#!/bin/bash
set -e

cd "$WORKING_DIR" || {
    echo "Error moving to the application's root directory."
    exit 1
}

### ASK TO PRUNE DOCKER SYSTEM
"$WORKING_DIR"/utils/scripts/common/docker-system-prune.sh

echo ""
echo "STARING DOCKER COMPOSE..."
echo ""
docker compose up --build
