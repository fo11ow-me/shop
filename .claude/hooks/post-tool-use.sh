#!/bin/bash
# PostToolUse: tracks Write/Edit file changes to a temp log
input=$(cat)

tool_name=$(echo "$input" | python -c "import sys,json; print(json.load(sys.stdin).get('tool_name',''))" 2>/dev/null)
tool_input=$(echo "$input" | python -c "import sys,json; print(json.dumps(json.load(sys.stdin).get('tool_input',{})))" 2>/dev/null)

if [ "$tool_name" = "Write" ] || [ "$tool_name" = "Edit" ]; then
  file_path=$(echo "$tool_input" | python -c "import sys,json; print(json.load(sys.stdin).get('file_path',''))" 2>/dev/null)

  if [ -n "$file_path" ] && [ "$file_path" != "None" ]; then
    echo "$file_path" >> "D:/project/idea/mall/.claude/hooks/.changed-files"
  fi
fi

echo '{"continue":true}'
