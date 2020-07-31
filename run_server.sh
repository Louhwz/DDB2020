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
make runregistry &
make runtm &
make runrmflights &
make runrmrooms &
make runrmcars &
make runrmcustomers &
make runwc &

