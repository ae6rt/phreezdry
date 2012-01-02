#!/bin/sh

set -u

# output cache stats
curl -i -d "key=foo" -X PUT http://api.phreezdry.com/services/admin/dumpstats
