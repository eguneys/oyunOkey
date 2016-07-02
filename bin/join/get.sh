#!/bin/sh

command=$1
user=$2
data=$3

if [ -x $user ] || [ -x $command ]; then
    echo "./get command user"
    exit 1
fi


url=192.168.56.103/$command
cookie=/tmp/$user
headers='-H "X-Requested-With: XMLHttpRequest" -H "Accept: application/vnd.oyunkeyf.v1+json"'
params="$headers -b $cookie -o /dev/null"

curl -X GET $url $params -v 2>&1

echo "curl -X GET $url $params as $user"
