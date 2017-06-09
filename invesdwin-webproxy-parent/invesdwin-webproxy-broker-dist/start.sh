#!/bin/bash

#set command
TEMP_DIR=runtemp

#check if running in terminal
if [ -t 1 ]
then 
    #fix pwd
    BASEDIR=`dirname $0`
    cd $BASEDIR
    
    #run command
    rm -rf $TEMP_DIR/
    unzip dist/*.zip -d $TEMP_DIR > /dev/null
    sh $TEMP_DIR/start.sh
    rm -rf $TEMP_DIR/
    
    #maybe keep terminal open
    if test -n $1
    then
        echo
        echo "Press ENTER to close..."
        read
    fi
    
else
    #open a new terminal
    x-terminal-emulator -e $0 $REAL_USER
fi

