#!/bin/sh
cd src/server && rmiregistry &
for i in 1 2 3 4 5
do
    gnome-terminal -e "make server arguments='1.0 $i dbs$i 224.0.0.1 4445'"
done