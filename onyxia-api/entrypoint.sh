#!/bin/bash

# Import CA certificates to Java keystore
if [[ -n "$CACERTS_DIR" ]]; then
  for file in $CACERTS_DIR/*
  do
    echo "Adding $file to keystore"
    keytool -import -cacerts -trustcacerts -noprompt -storepass changeit -alias $(basename $file) -file $file
  done
fi

# Run application
java -jar /app.jar
