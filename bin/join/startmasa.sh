#!/bin/sh

url=192.168.56.103/masa/new
data="rounds=5&variant=1"

#mid=`curl -X POST $url -d $data -v --silent 2>&1 | grep 'mk2=\K([^\;]*)' -oP`
mid=`curl -X POST 192.168.56.103/masa/new -d 'rounds=5&variant=1' --silent 2>&1 -v | grep 'Location: /masa/\K(.{8})' -oP`

echo $mid
