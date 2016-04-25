#!/bin/sh

mid=$1
url=192.168.56.103/masa/$mid/join

curl -X POST $url
curl -X POST $url
curl -X POST $url

echo "done"
