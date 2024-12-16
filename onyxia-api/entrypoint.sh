#!/bin/bash

# Import CA certificates to Java keystore
if [[ -n "$CACERTS_DIR" ]]; then
  for file in $CACERTS_DIR/*
  do
    if [ -f "$file" ]
    then
      echo "Adding $file to keystore"
      keytool -import -cacerts -trustcacerts -noprompt -storepass changeit -alias $(basename $file) -file $file
    fi
  done
fi

# Run application
if [ -n "$DEBUG_JMX" ]; then
  JMX_PORT="${JMX_PORT:-10000}"
  java -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=false org.springframework.boot.loader.launch.JarLauncher
else
  java org.springframework.boot.loader.launch.JarLauncher
fi