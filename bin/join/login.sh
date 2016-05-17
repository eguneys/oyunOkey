#!/bin/sh

user=$1
password=$2

if [ -x $user ]; then
    echo "Empty user"
    exit 1
fi

data="username=$user&password=$password"

url=192.168.56.103/login
cookie=/tmp/$user
params="--silent -D - -d $data -c $cookie -o /dev/null"

curl -X POST $url $params 2>&1

echo "logged in at"
echo $cookie
