#!/bin/bash

# Import CA certificates to Java keystore
for file in $CACERTS_DIR/*
do
  echo "Adding $file to keystore"
  keytool -import -cacerts -trustcacerts -noprompt -storepass changeit -alias $(basename $file) -file $file
done

# Run application
java -jar /app.jar
