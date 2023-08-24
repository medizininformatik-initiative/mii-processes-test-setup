#!/bin/sh

STATUS_CODE=$(curl --write-out '%{http_code}' --silent --output /dev/null "$1/metadata")

if [[ $STATUS_CODE -ne 200 ]] ; then
  echo "Could not reach FHIR store metadata using url '$1/metadata'"
  exit
fi

for file in data/*.json
do
  echo "Sending FHIR bundle $file ..."
  curl -X POST -H "Content-Type: application/json" -d @"$file" "$1"
done