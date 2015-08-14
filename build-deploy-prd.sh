#!/bin/bash
# we don't build the WAR because deploying a compressed WAR takes too much time
# better to build a standard stuff
mvn -DskipTests -DincludeScope=runtime clean install dependency:copy-dependencies
rsync --del -R -Pzrlt target/classes target/dependency config/*.dev.* README.md smartroad smartroadd smartroad-autofollow smartroad-followedsync smartroad-collector ceefour@luna3.bippo.co.id:smartroad/
