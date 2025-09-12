#!/bin/sh

ACCESS_TOKEN=$(curl -k -s -d 'grant_type=client_credentials' -u 'account:e11a3a8e-6e24-4f9d-b914-da7619e8b31f' \
https://localhost:8443/realms/blaze/protocol/openid-connect/token | jq -r .access_token)

STATUS_CODE=$(curl --oauth2-bearer "${ACCESS_TOKEN}" --write-out '%{http_code}' --silent --output /dev/null "$1/metadata")

if [[ $STATUS_CODE -ne 200 ]] ; then
  echo "Could not reach FHIR store metadata using url '$1/metadata'"
  exit
fi

for file in data/*.json
do
  echo ""
  echo "Sending FHIR bundle $file ..."

  ACCESS_TOKEN=$(curl -k -s -d 'grant_type=client_credentials' -u 'account:e11a3a8e-6e24-4f9d-b914-da7619e8b31f' \
  https://localhost:8443/realms/blaze/protocol/openid-connect/token | jq -r .access_token)

  curl -X POST -H "Content-Type: application/json" --oauth2-bearer "${ACCESS_TOKEN}" -d @"$file" "$1"
done