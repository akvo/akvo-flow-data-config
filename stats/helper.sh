#!/bin/sh

find "$1" -name 'appengine-web.xml' -exec grep '<application>' {} \; | sed 's/<[/]*application>//g' | sed 's/^[ \t]*//;s/[ \t]*$//' | sort > /tmp/instances.txt

cd "$2"
python stats.py "$3" "$4" /tmp/instances.txt

mkdir -p /tmp/akvo/flow/reports/stats
mv /tmp/stats.txt /tmp/akvo/flow/reports/stats/$(date +"%Y-%m-%d").csv

exit 0
