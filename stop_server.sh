#!/usr/bin/env bash

ps -ef|grep transaction|grep -v grep|cut -c 8-13|xargs kill -9

kill $(lsof -t -i:3345)
