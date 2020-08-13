#!/usr/bin/env bash


# make server
cd src/transaction || exit

rm -rf data/

# run registry
make runtm &
make runrmflights &
make runrmrooms &
make runrmcars &
make runrmcustomers &
make runwc &


