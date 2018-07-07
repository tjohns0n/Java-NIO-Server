#!/bin/bash
CLASSES="~/Documents/CS455/src"
SCRIPT="cd $CLASSES;
java cs455.scaling.client.Client columbus-oh 1080 4"
for j in {1..10}
do
	COMMAND='gnome-terminal'
	for i in `cat machine_list` 
	do
		echo "logging into $i"
		OPTION=" --tab -e 'ssh -t "$i" "$SCRIPT"'"
		echo $OPTION
		COMMAND+="$OPTION"
	done
	eval $COMMAND &
done
