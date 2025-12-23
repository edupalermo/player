#!/usr/bin/env bash
set -e

Xvfb :99 -screen 0 1920x1080x24 &
sleep 2
x11vnc -display :99 -nopw -forever 

