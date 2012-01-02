#!/bin/sh

set -u
set -e

SVN_REVISION=$(svn up > /dev/null 2>&1; svn info |grep ^Revision |awk '{print $2}')
BUILD_DATE=$(date)

mvn -D SVN_REVISION="$SVN_REVISION" -D BUILD_DATE="$BUILD_DATE" clean package
