#!/bin/sh

find /opt/akvo/flow/repo/akvo-flow-server-config/ -name 'appengine-web.xml' -exec grep '<application>' {} \; | sed 's/<[/]*application>//g' | sed 's/^[ \t]*//;s/[ \t]*$//' | sort > /tmp/instances.txt

python stats.py "$1" "$2" /tmp/instances.txt

mkdir -p /tmp/akvo/flow/reports/stats
cp stats.txt /tmp/akvo/flow/reports/stats/$(date +"%Y-%m-%d").csv

exit 0
