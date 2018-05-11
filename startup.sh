#!/bin/bash	
DEST=target/scala-2.12/

sbt assemblyAll
rm -f /tmp/store

tmux new-session "java -jar register/${DEST}register.jar" \; \
     split-window -v "sleep 4; export REMOTEHOST=127.0.0.12; export MANAGEMENTPORT=10002; java -jar ${DEST}seedless.jar" \; \
     split-window -h "sleep 6; export REMOTEHOST=127.0.0.12; export MANAGEMENTPORT=10003; java -jar ${DEST}seedless.jar" \; \
     select-pane -t 0 \; \
     split-window -h  "sleep 8; export REMOTEHOST=127.0.0.16; java -jar ${DEST}seedless.jar" \;
#     select-pane -t 0 \;
     	