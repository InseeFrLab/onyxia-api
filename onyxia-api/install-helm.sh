#! /bin/bash
HELM_VERSION=v3.18.4

# initOS discovers the operating system for this system.
initOS() {
  OS=$(echo `uname`|tr '[:upper:]' '[:lower:]')

  case "$OS" in
    # Minimalist GNU for Windows
    mingw*|cygwin*) OS='windows';;
  esac
}

# initArch discovers the architecture for this system.
initArch() {
  ARCH=$(uname -m)
  case $ARCH in
    armv5*) ARCH="armv5";;
    armv6*) ARCH="armv6";;
    armv7*) ARCH="arm";;
    aarch64) ARCH="arm64";;
    x86) ARCH="386";;
    x86_64) ARCH="amd64";;
    i686) ARCH="386";;
    i386) ARCH="386";;
  esac
}

initOS
initArch
HELM_DIST="helm-${HELM_VERSION}-${OS}-${ARCH}.tar.gz"
wget https://get.helm.sh/${HELM_DIST}
tar -zxvf ${HELM_DIST}
mv ${OS}-${ARCH}/helm /usr/local/bin/helm
rm ${HELM_DIST}
rm -rf ${OS}-${ARCH}

