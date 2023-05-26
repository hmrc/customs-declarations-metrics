#!/usr/bin/env bash

sbt clean scalastyle coverage testAll coverageReport dependencyUpdates
