#!/bin/sh
make compile
cd bin && rmiregistry &
for i in 1 2 3 4 5
do
    gnome-terminal -e "make arguments='1.0 $i dbs$i 224.0.0.1 5555'"
done
