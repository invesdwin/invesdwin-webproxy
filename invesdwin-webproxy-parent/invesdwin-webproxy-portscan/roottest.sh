#!/bin/bash

#set user
if test -n $1
then
    REAL_USER=$1
else
    REAL_USER=$USER
fi

#set command
COMMAND="ant test ; chown $REAL_USER:$REAL_USER -R ../../"

#check if running in terminal
if [ -t 1 ]
then 
    #fix pwd for chown
    BASEDIR=`dirname $0`
    cd $BASEDIR
    
    #run command with su
    sudo su -c "$COMMAND"
    
    #maybe keep terminal open
    if test -n $1
    then
        echo
        echo "Press ENTER to close..."
        read
    fi
    
else
    #open a new terminal
    gksu "x-terminal-emulator -e $0 $REAL_USER"
fi

