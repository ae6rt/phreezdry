#!/bin/sh

# publish a document and verify it can be retrieved

set -u
#set -x

document="blarg"
headerFile=/tmp/headers.txt
rm -f $headerFile

curl -D $headerFile -s --user mark@petrovic.org:hoohoo -X PUT -d "document=$document" http://api.phreezdry.com/services/dry

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
