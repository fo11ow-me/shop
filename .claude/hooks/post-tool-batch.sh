#!/bin/bash
# PostToolBatch: compile check on Java changes + lightweight API smoke test
MALL_ROOT="D:/project/idea/mall"
tracking_file="$MALL_ROOT/.claude/hooks/.changed-files"

java_changed=false

if [ -f "$tracking_file" ]; then
  if grep -qiE '\.java$' "$tracking_file" 2>/dev/null; then
    java_changed=true
  fi
fi

# ---- Compile check ----
if $java_changed; then
  echo "::notice::Java files changed, running mvn compile..."
  output=$(cd "$MALL_ROOT/mall-server" && mvn compile -q 2>&1)
  exit_code=$?

  if [ $exit_code -ne 0 ]; then
    echo "::error::Compilation failed!"
    # Show first 15 lines of errors
    echo "$output" | grep -E 'ERROR|error:' | head -15
    echo '{"continue":true}'
    exit 0
  else
    echo "::notice::Compilation passed"
  fi
fi

echo '{"continue":true}'
