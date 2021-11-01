#!/bin/bash

decrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --quiet --batch --yes --decrypt --passphrase="$PASSPHRASE" --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  # Decrypt Release key
  decrypt ${ENCRYPT_KEY} signing/app-release.gpg signing/app-release.jks

else
  echo "ENCRYPT_KEY is empty"
  exit 22
fi