#!/bin/bash
rm -rf ptt.db
rm -rf ptt/index/*
mvn clean compile exec:java -Dexec.mainClass=com.ptt.Search
