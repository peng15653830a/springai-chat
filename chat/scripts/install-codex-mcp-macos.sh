#!/usr/bin/env bash
set -euo pipefail

# This installer fills sensitive placeholders from environment variables
# and writes the resulting config to ~/.config/codex/config.json.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_FILE="${SCRIPT_DIR}/codex-mcp-config.template.json"

DEST_DIR="${HOME}/.config/codex"
DEST_FILE="${DEST_DIR}/config.json"
TS="$(date +"%Y%m%d-%H%M%S")"

required_vars=(
  POSTGRES_URL
  MYSQL_HOST
  MYSQL_PORT
  MYSQL_USER
  MYSQL_PASSWORD
  MYSQL_DATABASE
)

missing=()
for v in "${required_vars[@]}"; do
  if [ -z "${!v-}" ]; then
    missing+=("$v")
  fi
done

if [ ${#missing[@]} -ne 0 ]; then
  echo "[ERROR] 缺少必要环境变量: ${missing[*]}" >&2
  echo "请先在当前 shell 设置并重试，例如：" >&2
  echo "  export POSTGRES_URL='postgresql://root:你的密码@localhost:5432/ai_chat'" >&2
  echo "  export MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=你的密码 MYSQL_DATABASE=ai_chat" >&2
  exit 1
fi

escape_sed() {
  # escape for sed replacement: /, &, and backslashes
  printf '%s' "$1" | sed -e 's/[\/&]/\\&/g'
}

POSTGRES_URL_ESCAPED="$(escape_sed "${POSTGRES_URL}")"
MYSQL_HOST_ESCAPED="$(escape_sed "${MYSQL_HOST}")"
MYSQL_PORT_ESCAPED="$(escape_sed "${MYSQL_PORT}")"
MYSQL_USER_ESCAPED="$(escape_sed "${MYSQL_USER}")"
MYSQL_PASSWORD_ESCAPED="$(escape_sed "${MYSQL_PASSWORD}")"
MYSQL_DATABASE_ESCAPED="$(escape_sed "${MYSQL_DATABASE}")"

mkdir -p "${DEST_DIR}"

TMP_FILE="$(mktemp)"
sed \
  -e "s|{{POSTGRES_URL}}|${POSTGRES_URL_ESCAPED}|g" \
  -e "s|{{MYSQL_HOST}}|${MYSQL_HOST_ESCAPED}|g" \
  -e "s|{{MYSQL_PORT}}|${MYSQL_PORT_ESCAPED}|g" \
  -e "s|{{MYSQL_USER}}|${MYSQL_USER_ESCAPED}|g" \
  -e "s|{{MYSQL_PASSWORD}}|${MYSQL_PASSWORD_ESCAPED}|g" \
  -e "s|{{MYSQL_DATABASE}}|${MYSQL_DATABASE_ESCAPED}|g" \
  "${TEMPLATE_FILE}" > "${TMP_FILE}"

if [ -f "${DEST_FILE}" ]; then
  cp "${DEST_FILE}" "${DEST_FILE}.bak.${TS}"
  if command -v jq >/dev/null 2>&1; then
    MERGED="$(mktemp)"
    jq -s '
      (.[1] // {}) as $old | (.[0] // {}) as $new |
      $old + { mcpServers: (($old.mcpServers // {}) + ($new.mcpServers // {})) }
    ' "${TMP_FILE}" "${DEST_FILE}" > "${MERGED}" && mv "${MERGED}" "${DEST_FILE}"
  else
    echo "未检测到 jq，无法智能合并，直接覆盖现有配置。建议：brew install jq 以支持合并。" >&2
    mv "${TMP_FILE}" "${DEST_FILE}"
  fi
else
  mv "${TMP_FILE}" "${DEST_FILE}"
fi

echo "已写入: ${DEST_FILE}"
if [ -f "${DEST_FILE}.bak.${TS}" ]; then
  echo "已备份: ${DEST_FILE}.bak.${TS}"
fi

echo
echo "提示：部分服务器需要在 shell 中准备 API Key（例如 EXA、GitHub、Notion）。"
echo "你可以在 ~/.zshrc 中 export 对应变量，或保持 config 中的 env 配置为空以继承当前环境。"

