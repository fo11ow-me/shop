@echo off
set "PATH=D:\software\Git\usr\bin;%PATH%"
bash "%~dp0deploy\docker-down.sh" %*
