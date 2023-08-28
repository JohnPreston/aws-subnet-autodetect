#!/bin/bash
set -ex
export JAVA_OPTS=${JAVA_OPTS:-$JAVA_DEFAULT_OPTS}
exec java $EXTRA_ARGS "$@"
