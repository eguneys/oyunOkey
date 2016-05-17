#!/bin/sh

command=$1
data=$2
user=$3

if [ -x $user ] || [ -x $command ] || [ -x $data ]; then
    echo "./post command data user"
    exit 1
fi


url=192.168.56.103/$command
cookie=/tmp/$user
params="--silent -D - -d $data -b $cookie -o /dev/null"

curl -X POST $url $params 2>&1

echo "curl -X POST $url $params as $user"
