#!/bin/bash

#script a copier dans InitSibyl une fois le tar.gz decompresse
# to keep files on emulator
dir=/data/music
if [ $# -ne "1" ] 
then
 echo "Error:$0 adb_command";
 exit 1;
fi;

echo "Add mp3..."
for f in *.mp3
do
 $1 push $f $dir/$f
done

echo "Add covers..."
for f in *.jpg
do
 $1 push $f $dir/$f
done
