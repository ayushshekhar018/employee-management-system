#!/bin/sh
set -eu

# Render provides PORT; local Docker runs default to Tomcat's usual HTTP port.
PORT="${PORT:-10000}"

# Configure Tomcat's HTTP connector to listen on the port expected by Render.
sed -i "s/port=\"8080\"/port=\"$PORT\"/" "$CATALINA_HOME/conf/server.xml"

exec catalina.sh run
