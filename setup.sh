#!/bin/sh
$inicial=1
$final=10
for i in 'seq $inicial $final'
do
    gnome-terminal -x sh -c 'make server arguments="1 $i 3 224.0.0.1 4445"'
done