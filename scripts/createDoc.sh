#!/bin/sh

# publish a document and verify it can be retrieved

set -u
#set -x

host="api.phreezdry.com"
#host="localhost:8080"
#context="/dry"
context=""
document="yet another test doc"
headerFile=/tmp/headers.txt
rm -f $headerFile

echo "host: $host, context: $context"

curl -D $headerFile -s --user mark@petrovic.org:whatisthis -X PUT -d "document=$document" http://${host}${context}/services/dry

created=$(grep ^HTTP $headerFile| awk '{print $2}')
if [ "$created" != "201" ]; then
   echo "- expecting HTTP 201 Created:  got $created"
   exit -1
else
   echo "- 201 Created"
fi

location=$(grep ^Location $headerFile | sed -e 's///' | awk '{print $2}')
retrievedDocument=$(curl -s $location)

if [ "$document" != "$retrievedDocument" ]; then
   echo "- document are not the same error"
else
   echo "- 200 OK:  original and retrieved documents match"
fi
