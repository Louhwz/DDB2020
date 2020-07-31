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
make run registry &
make run tm &
make runrmflights &


