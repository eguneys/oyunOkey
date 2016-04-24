#!/bin/sh

mid=$1
url=192.168.56.103/masa/$mid/join

curl -s -X POST $url > /dev/null
curl -s -X POST $url > /dev/null
curl -s -X POST $url > /dev/null

echo "done"
