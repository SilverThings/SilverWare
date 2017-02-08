#!/bin/zsh
DIR=target_release

function prepare_files() {
   rm -rf $DIR
   mkdir $DIR
   find -name "*.asc" > $DIR/list
   for i in $(cat $DIR/list); do
	  cp $i $DIR/
	  cp ${i:r} $DIR/
   done
   rm -f $DIR/list
}

function compress_files() {
   find $DIR -name "*.pom" > $DIR/compress
   for i in $(cat $DIR/compress); do
   	  rm -f ${i:r}.zip
   	  zip -9qjr ${i:r}.zip ${i:r}*
   	  echo "Creating ${i:r}.zip... ok"
   done
   rm -f $DIR/compress
}

prepare_files
compress_files