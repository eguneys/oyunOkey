#!/bin/sh

mid=$1
host=localhost:8080
url=$host/masa/$mid/join
params=--silent

curl -X POST $url $params | head
curl -X POST $url $params | head
curl -X POST $url $params | head

echo "autojoined"
echo $mid
