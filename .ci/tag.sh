#!/usr/bin/env bash

set -e

export DOCKER_TAG="improve-ci"

if [ "$TRAVIS_TAG" != "" ];then
  export DOCKER_TAG=$TRAVIS_TAG
fi

echo $DOCKER_TAG