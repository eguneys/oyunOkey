#!/bin/sh

mid=$1
url=192.168.56.103/masa/$mid/join
params=--silent

curl -X POST $url $params | head
curl -X POST $url $params | head
curl -X POST $url $params | head

echo "autojoined"
echo $mid
