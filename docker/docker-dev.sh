#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV="${1:-dev}"
ACTION="${2:-up}"

show_help() {
  cat <<'EOF'
用法:
  ./docker-dev.sh [env] [action] [extra args...]

默认值:
  env=dev
  action=up

支持动作:
  build      构建镜像
  up         后台启动容器
  build-up   构建并后台启动（build + up -d）
  down       停止并清理容器
  restart    重启服务（down + up -d）
  logs       查看并跟踪日志

示例:
  ./docker-dev.sh
  ./docker-dev.sh dev build-up
  ./docker-dev.sh test logs service-system-admin
EOF
}

resolve_compose_file() {
  local target_env="$1"
  local candidate
  local candidates=(
    "${SCRIPT_DIR}/compose/docker-compose.${target_env}.yml"
    "${SCRIPT_DIR}/${target_env}/docker-compose.yml"
  )

  for candidate in "${candidates[@]}"; do
    if [[ -f "${candidate}" ]]; then
      printf '%s\n' "${candidate}"
      return 0
    fi
  done
  return 1
}

if [[ "${ENV}" == "help" || "${ENV}" == "-h" || "${ENV}" == "--help" ]]; then
  show_help
  exit 0
fi

if ! COMPOSE_FILE="$(resolve_compose_file "${ENV}")"; then
  echo "错误: 环境 '${ENV}' 对应的 compose 文件不存在。"
  echo "已检查路径:"
  echo "  - ${SCRIPT_DIR}/compose/docker-compose.${ENV}.yml"
  echo "  - ${SCRIPT_DIR}/${ENV}/docker-compose.yml"
  exit 1
fi

shift 2 || true

case "${ACTION}" in
  build)
    docker compose -f "${COMPOSE_FILE}" build "$@"
    ;;
  up)
    docker compose -f "${COMPOSE_FILE}" up -d "$@"
    ;;
  build-up)
    docker compose -f "${COMPOSE_FILE}" build "$@"
    docker compose -f "${COMPOSE_FILE}" up -d "$@"
    ;;
  down)
    docker compose -f "${COMPOSE_FILE}" down "$@"
    ;;
  restart)
    docker compose -f "${COMPOSE_FILE}" down "$@"
    docker compose -f "${COMPOSE_FILE}" up -d "$@"
    ;;
  logs)
    docker compose -f "${COMPOSE_FILE}" logs -f "$@"
    ;;
  *)
    echo "错误: 不支持的动作 '${ACTION}'。"
    show_help
    exit 1
    ;;
esac
