#!/bin/sh

find /opt/akvo/flow/repo/akvo-flow-server-config/ -name 'appengine-web.xml' -exec grep '<application>' {} \; | sed 's/<[/]*application>//g' | sed 's/^[ \t]*//;s/[ \t]*$//' | sort > /tmp/instances.txt

python stats.py "$1" "$2" /tmp/instances.txt

exit 0
