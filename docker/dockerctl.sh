#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/compose/docker-compose.yml"
SUPPORTED_ENVS=(dev test prod)

show_help() {
  cat <<'EOF'
用法:
  ./dockerctl.sh <env> <action> [extra args...]

说明:
  共用一份 compose；<env> 决定 Spring profile 与 docker/env/<env>/ 密钥文件。
  宿主机仅暴露网关 8080。

支持环境:
  dev | test | prod

支持动作:
  build      构建镜像
  up         后台启动容器
  build-up   构建并后台启动（build + up -d）
  down       停止并清理容器
  restart    重启服务（down + up -d）
  logs       查看并跟踪日志
  ps         查看容器状态
  config     校验并输出 compose 配置

示例:
  ./dockerctl.sh dev up
  ./dockerctl.sh prod build-up
  ./dockerctl.sh prod logs -f auth-gateway

说明:
  compose 已默认 pull: false，优先使用本机已有基础镜像。
  推 ACR 时可预先 export AUTH_IMAGE_PREFIX / AUTH_IMAGE_TAG。
EOF
}

is_supported_env() {
  local target="$1"
  local item
  for item in "${SUPPORTED_ENVS[@]}"; do
    if [[ "${item}" == "${target}" ]]; then
      return 0
    fi
  done
  return 1
}

default_image_tag() {
  case "$1" in
    prod) printf 'local-prod\n' ;;
    test) printf 'local-test\n' ;;
    *) printf 'local\n' ;;
  esac
}

prepare_env() {
  local target_env="$1"
  local env_file="${SCRIPT_DIR}/env/${target_env}/service-system.env"

  if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "错误: compose 文件不存在: ${COMPOSE_FILE}"
    exit 1
  fi
  if [[ ! -f "${env_file}" ]]; then
    echo "错误: 缺少环境文件: ${env_file}"
    echo "请复制 ${env_file}.example 为 service-system.env 后填写。"
    exit 1
  fi

  export AUTH_ENV="${target_env}"
  export AUTH_IMAGE_TAG="${AUTH_IMAGE_TAG:-$(default_image_tag "${target_env}")}"
  export SPRING_CLOUD_NACOS_SERVER_ADDR="${SPRING_CLOUD_NACOS_SERVER_ADDR:-nacos:8848}"
}

dc() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

if [[ "${1:-}" == "help" || "${1:-}" == "-h" || "${1:-}" == "--help" || $# -lt 2 ]]; then
  show_help
  exit 0
fi

TARGET_ENV="$1"
ACTION="$2"
shift 2

if ! is_supported_env "${TARGET_ENV}"; then
  echo "错误: 不支持的环境 '${TARGET_ENV}'（仅支持: ${SUPPORTED_ENVS[*]}）"
  exit 1
fi

prepare_env "${TARGET_ENV}"

case "${ACTION}" in
  build)
    dc build "$@"
    ;;
  up)
    dc up -d "$@"
    ;;
  build-up)
    dc build "$@"
    # build-only flags must not be forwarded to up (e.g. --pull=false, --no-cache)
    up_args=()
    skip_next=0
    for arg in "$@"; do
      if [[ "${skip_next}" -eq 1 ]]; then
        skip_next=0
        continue
      fi
      case "${arg}" in
        --no-cache|--pull|--pull=*|--quiet|-q|--parallel|--with-dependencies|--builder|--builder=*|--progress|--progress=*|--build-arg=*|--ssh|--ssh=*|--memory|--memory=*)
          continue
          ;;
        --build-arg|--progress|--builder|--ssh|--memory)
          skip_next=1
          continue
          ;;
      esac
      up_args+=("${arg}")
    done
    dc up -d "${up_args[@]}"
    ;;
  down)
    dc down "$@"
    ;;
  restart)
    dc down "$@"
    dc up -d "$@"
    ;;
  logs)
    dc logs -f "$@"
    ;;
  ps)
    dc ps "$@"
    ;;
  config)
    dc config "$@"
    ;;
  help|-h|--help)
    show_help
    ;;
  *)
    echo "错误: 不支持的动作 '${ACTION}'"
    show_help
    exit 1
    ;;
esac
