#!/usr/bin/env bash
set -eu

start_docker() {
    # Check if Docker daemon is already running
    if pgrep -x dockerd > /dev/null; then
        echo "Docker daemon is already running."
        return 0
    fi

    # Clean up stale socket if it exists without a running daemon
    if [ -S /var/run/docker.sock ]; then
        sudo rm -f /var/run/docker.sock
    fi

    # Clean up stale pid file if it exists
    if [ -f /var/run/docker.pid ]; then
        sudo rm -f /var/run/docker.pid
    fi

    # Start Docker daemon in the background
    sudo dockerd &

    # Wait until the Docker socket is available
    timeout=30
    while [ ! -S /var/run/docker.sock ] && [ "$timeout" -gt 0 ]; do
        sleep 1
        timeout=$((timeout - 1))
    done

    if [ -S /var/run/docker.sock ]; then
        echo "Docker daemon is ready."
    else
        echo "Warning: Docker daemon did not start within 30 seconds." >&2
        return 1
    fi
}

# Start Docker daemon
start_docker

# Monitor and restart Docker daemon if it stops
while true; do
    if ! pgrep -x dockerd > /dev/null; then
        echo "Docker daemon stopped, restarting..."
        start_docker
    fi
    sleep 5
done
