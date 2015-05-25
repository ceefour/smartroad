#!/bin/bash
# we don't build the WAR because deploying a compressed WAR takes too much time
# better to build a standard stuff
mvn -DskipTests clean install dependency:copy-dependencies
rsync --del -R -Pzrlt target/classes target/dependency config/*.dev.* README.md smartroad smartroadd u0016139@172.29.160.116:smartroad/
