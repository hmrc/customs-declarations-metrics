#!/usr/bin/env bash

sbt clean coverage testAll coverageReport dependencyUpdates
