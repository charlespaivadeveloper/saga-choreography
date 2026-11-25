#!/usr/bin/env bash
# Wrapper: delegates to k8s/deploy.sh from the project root.
# Usage: ./scripts/deploy-kind.sh [cluster|build|load|infra|services|kong|monitoring|all]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}/../k8s"
exec ./deploy.sh "$@"
