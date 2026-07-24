#!/usr/bin/env bash
# 本机从同级 Auth/assets 打三个 auth-kit 包，上传到本仓库（auth-server）的 GitHub Release。
# assets 源码不进 GitHub，仅作为 Release 附件；版本与后端 tag 一致。
#
# 用法:
#   ./scripts/upload-auth-kit-release.sh v1.0.0
#   ./scripts/upload-auth-kit-release.sh v1.0.0 --save-images  # 缺 tar.gz 时先 docker save
#   ./scripts/upload-auth-kit-release.sh v1.0.0 --create       # Release 不存在则创建
#
# 前置: 同级存在 ../assets；已安装并登录 gh；打 tag 后再上传（或加 --create）。
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ASSETS_DIR="$(cd "${REPO_DIR}/../assets" && pwd)"
PACK_SCRIPT="${ASSETS_DIR}/pack-assets.sh"
SAVE_SCRIPT="${ASSETS_DIR}/docker/scripts/save-images.sh"

TAG=""
DO_SAVE_IMAGES=0
DO_CREATE=0

usage() {
  echo "用法: $0 <tag> [--save-images] [--create]" >&2
  echo "  例: $0 v1.0.0" >&2
  echo "  例: $0 v1.0.0 --save-images --create" >&2
  exit 1
}

if [[ $# -lt 1 ]]; then
  usage
fi

TAG="$1"
shift

while [[ $# -gt 0 ]]; do
  case "$1" in
    --save-images) DO_SAVE_IMAGES=1 ;;
    --create) DO_CREATE=1 ;;
    -h|--help) usage ;;
    *)
      echo "未知参数: $1" >&2
      usage
      ;;
  esac
  shift
done

if [[ ! -x "${PACK_SCRIPT}" ]]; then
  echo "错误: 找不到可执行的 ${PACK_SCRIPT}（需与 auth-server 同级的 assets）" >&2
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "错误: 未安装 gh（GitHub CLI）" >&2
  exit 1
fi

if [[ "${DO_SAVE_IMAGES}" -eq 1 ]]; then
  if [[ ! -x "${SAVE_SCRIPT}" ]]; then
    echo "错误: 找不到 ${SAVE_SCRIPT}" >&2
    exit 1
  fi
  echo "导出离线镜像到 assets/docker/images/{middleware,build}/ ..."
  "${SAVE_SCRIPT}" --images-middleware --images-build
fi

DIST_DIR="$(mktemp -d)"
trap 'rm -rf "'"${DIST_DIR}"'"' EXIT

echo "打包 auth-kit（版本 ${TAG}）→ ${DIST_DIR}"
OUTPUT_DIR="${DIST_DIR}" "${PACK_SCRIPT}" "${TAG}"
OUTPUT_DIR="${DIST_DIR}" "${PACK_SCRIPT}" --images-middleware "${TAG}"
OUTPUT_DIR="${DIST_DIR}" "${PACK_SCRIPT}" --images-build "${TAG}"

BOOTSTRAP="${DIST_DIR}/auth-kit-bootstrap-${TAG}.tar.gz"
MIDDLEWARE="${DIST_DIR}/auth-kit-images-middleware-${TAG}.tar.gz"
BUILD="${DIST_DIR}/auth-kit-images-build-${TAG}.tar.gz"

for f in "${BOOTSTRAP}" "${MIDDLEWARE}" "${BUILD}"; do
  if [[ ! -f "${f}" ]]; then
    echo "错误: 缺少产物 ${f}" >&2
    exit 1
  fi
  echo "  $(du -h "${f}" | awk '{print $1}')  $(basename "${f}")"
done

cd "${REPO_DIR}"

if gh release view "${TAG}" >/dev/null 2>&1; then
  echo "上传到已有 Release ${TAG} ..."
  gh release upload "${TAG}" \
    "${BOOTSTRAP}" \
    "${MIDDLEWARE}" \
    "${BUILD}" \
    --clobber
elif [[ "${DO_CREATE}" -eq 1 ]]; then
  echo "创建 Release ${TAG} 并上传附件 ..."
  gh release create "${TAG}" \
    "${BOOTSTRAP}" \
    "${MIDDLEWARE}" \
    "${BUILD}" \
    --title "${TAG}" \
    --notes "auth-kit 附件：bootstrap / images-middleware / images-build（与后端 ${TAG} 同步）。"
else
  echo "错误: Release ${TAG} 不存在。先打 tag 并创建 Release，或加 --create。" >&2
  echo "  git tag -a ${TAG} -m \"${TAG}\" && git push origin ${TAG}" >&2
  echo "  或: $0 ${TAG} --create" >&2
  exit 1
fi

echo "完成: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/releases/tag/${TAG}"
