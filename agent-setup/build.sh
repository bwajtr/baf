#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="baf-agent-sandbox:latest"

echo "Building $IMAGE_NAME..."
echo ""

container build \
    --memory 8GB \
    --tag "$IMAGE_NAME" \
    -f "./Dockerfile"

echo ""
echo "Successfully built $IMAGE_NAME"
