#!/usr/bin/env bash

# make lock
cd src/lockmgr || exit
make clean
make

# make server
cd ../transaction || exit
make clean
make server

# run registry
kill $(lsof -t -i:3345)
ps -ef|grep runtm|grep -v grep|cut -c 8-13|xargs kill -9
ps -ef|grep runrmflights|grep -v grep|cut -c 8-13|xargs kill -9
ps -ef|grep runrmrooms|grep -v grep|cut -c 8-13|xargs kill -9
ps -ef|grep runrmcars|grep -v grep|cut -c 8-13|xargs kill -9
ps -ef|grep runrmcustomers|grep -v grep|cut -c 8-13|xargs kill -9
ps -ef|grep runwc|grep -v grep|cut -c 8-13|xargs kill -9

make runregistry &
make runtm &
make runrmflights &
make runrmrooms &
make runrmcars &
make runrmcustomers &
make runwc &

make clean
