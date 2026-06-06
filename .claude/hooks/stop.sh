#!/bin/bash
# Stop: prints a summary of changed files this session
tracking_file="D:/project/idea/mall/.claude/hooks/.changed-files"

if [ -f "$tracking_file" ]; then
  total=$(wc -l < "$tracking_file" | tr -d ' ')
  if [ "$total" -gt 0 ]; then
    echo ""
    echo "=== Session Changes Summary ==="
    echo "Files modified ($total total):"
    sort "$tracking_file" | uniq -c | sort -rn | while read count file; do
      module=$(echo "$file" | grep -oE 'mall-(admin|portal|server)' || echo "other")
      printf "  [%s] x%2s  %s\n" "$module" "$count" "$file"
    done
    echo ""
  fi
  # Reset for next session
  rm -f "$tracking_file"
fi

echo '{"continue":true}'
