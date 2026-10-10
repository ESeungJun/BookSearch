#!/bin/sh
# 이 기기에서 이 저장소를 처음 연 세션인지 온보딩 에이전트에 알린다.
# 처음이면 표시 파일(.claude/.onboarded, git 에 올리지 않음)을 만든다. 파일을 하나 만드는 것 외에는 아무것도 하지 않는다.
marker="${CLAUDE_PROJECT_DIR:-.}/.claude/.onboarded"
if [ -f "$marker" ]; then
  context="SESSION_KIND=returning"
else
  touch "$marker" 2>/dev/null
  context="SESSION_KIND=first"
fi
printf '{"hookSpecificOutput":{"hookEventName":"SessionStart","additionalContext":"%s"}}\n' "$context"
