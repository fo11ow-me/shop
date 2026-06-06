#!/bin/bash
# PreToolUse: blocks dangerous commands + warns on cross-module edits
input=$(cat)

tool_name=$(echo "$input" | python -c "import sys,json; print(json.load(sys.stdin).get('tool_name',''))" 2>/dev/null)
tool_input=$(echo "$input" | python -c "import sys,json; print(json.dumps(json.load(sys.stdin).get('tool_input',{})))" 2>/dev/null)

# ---- Dangerous Bash command detection ----
if [ "$tool_name" = "Bash" ]; then
  cmd=$(echo "$tool_input" | python -c "import sys,json; print(json.load(sys.stdin).get('command',''))" 2>/dev/null)

  # Block destructive Git operations
  if echo "$cmd" | grep -qiE 'git push.*--force|git reset.*--hard|git branch.*-D'; then
    echo "::warning::BLOCKED destructive git command: $cmd"
    echo '{"decision":"block","reason":"Destructive git command blocked by PreToolUse hook"}'
    exit 0
  fi

  # Block dangerous filesystem operations
  if echo "$cmd" | grep -qiE 'rm -rf /[^t]|rm -rf [^.]*[/ ]\*|del /f /s'; then
    echo "::warning::BLOCKED dangerous rm command: $cmd"
    echo '{"decision":"block","reason":"Dangerous rm command blocked by PreToolUse hook"}'
    exit 0
  fi

  # Block dangerous database operations
  if echo "$cmd" | grep -qiE 'DROP (TABLE|DATABASE)|TRUNCATE TABLE|mysql.*-e.*DROP'; then
    echo "::warning::BLOCKED dangerous SQL command: $cmd"
    echo '{"decision":"block","reason":"Dangerous SQL command blocked by PreToolUse hook"}'
    exit 0
  fi
fi

# ---- Cross-module Write warning (informational only) ----
if [ "$tool_name" = "Write" ] || [ "$tool_name" = "Edit" ]; then
  file_path=$(echo "$tool_input" | python -c "import sys,json; print(json.load(sys.stdin).get('file_path',''))" 2>/dev/null)

  lower_path=$(echo "$file_path" | tr '[:upper:]' '[:lower:]')
  case "$lower_path" in
    *mall-admin*) module="mall-admin" ;;
    *mall-portal*) module="mall-portal" ;;
    *mall-server*) module="mall-server" ;;
    *) module="unknown" ;;
  esac

  echo "::notice::Editing [$module]: $file_path"
fi

echo '{"continue":true}'
