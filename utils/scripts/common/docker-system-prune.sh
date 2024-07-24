#!/bin/bash
set -e

echo ""
read -r -p "Do you want to prune your Docker system? [y/N] " prune_docker_system
prune_docker_system=$(echo "$prune_docker_system" | tr '[:upper:]' '[:lower:]')

if [[ "$prune_docker_system" =~ ^(y|yes)$ ]]; then
    echo ""
    echo "PRUNING DOCKER SYSTEM..."
    echo ""
    docker system prune --all --force --volumes
    echo "DONE!"

    echo ""
    echo "REMOVING ALL LOCAL VOLUMES..."
    echo ""
    actual_volumes=$(docker volume ls -q)
    for volume in $actual_volumes
    do
        if [ -n "$volume" ]; then
            docker volume rm "$volume"
        fi
    done
else
    echo ">> No problem. You can prune your Docker system later."
fi
