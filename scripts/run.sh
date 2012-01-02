#!/bin/sh

set -u
set -x

#heap="-Xmx256m"
heap=""
jetty=$HOME/jetty/jetty-6.1.20
cp target/dry.war $jetty/webapps/dry.war

(
cd $jetty
#mkdir -p work
#rm -rf work/*
#java -DDEBUG -jar start.jar
java -jar start.jar
)
