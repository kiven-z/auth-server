#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}/compose"

usage() {
  cat <<'EOF'
Usage:
  ./dockerctl.sh <env> <action> [extra docker compose args]

Environment:
  dev | test

Action:
  build       Build all services
  up          Start services in background
  down        Stop and remove services
  logs        Tail service logs
  ps          Show container status
  config      Validate and print merged compose config
  help        Show this message

Examples:
  ./dockerctl.sh dev build
  ./dockerctl.sh dev up
  ./dockerctl.sh test up
  ./dockerctl.sh dev down
  ./dockerctl.sh dev logs

Direct compose:
  docker compose -f docker/compose/docker-compose.dev.yml up -d
  docker compose -f docker/compose/docker-compose.dev.yml down
  docker compose -f docker/compose/docker-compose.test.yml up -d
  docker compose -f docker/compose/docker-compose.test.yml down
EOF
}

if [[ "${1:-}" == "" ]]; then
  usage
  exit 0
fi

TARGET_ENV="${1:-}"
ACTION="${2:-help}"
shift $(( $# > 0 ? 1 : 0 ))
shift $(( $# > 0 ? 1 : 0 ))
EXTRA_ARGS=("$@")

case "${TARGET_ENV}" in
  dev|test)
    ;;
  help|-h|--help)
    usage
    exit 0
    ;;
  *)
    echo "Error: unsupported environment '${TARGET_ENV}'."
    usage
    exit 1
    ;;
esac

ENV_COMPOSE_FILE="${COMPOSE_DIR}/docker-compose.${TARGET_ENV}.yml"
if [[ ! -f "${ENV_COMPOSE_FILE}" ]]; then
  echo "Error: compose file '${ENV_COMPOSE_FILE}' is missing."
  exit 1
fi

COMPOSE_ARGS=(-f "${ENV_COMPOSE_FILE}")

case "${ACTION}" in
  build)
    docker compose "${COMPOSE_ARGS[@]}" build "${EXTRA_ARGS[@]}"
    ;;
  up)
    docker compose "${COMPOSE_ARGS[@]}" up -d "${EXTRA_ARGS[@]}"
    ;;
  down)
    docker compose "${COMPOSE_ARGS[@]}" down "${EXTRA_ARGS[@]}"
    ;;
  logs)
    docker compose "${COMPOSE_ARGS[@]}" logs -f "${EXTRA_ARGS[@]}"
    ;;
  ps)
    docker compose "${COMPOSE_ARGS[@]}" ps "${EXTRA_ARGS[@]}"
    ;;
  config)
    docker compose "${COMPOSE_ARGS[@]}" config "${EXTRA_ARGS[@]}"
    ;;
  help|-h|--help)
    usage
    ;;
  *)
    echo "Error: unsupported action '${ACTION}'."
    usage
    exit 1
    ;;
esac
