#!/usr/bin/env bash

CC="clang"
OUT="day-06"
CFLAGS="-Wall -Wextra"
FILE="day_06.c"

$CC -o $OUT $CFLAGS $FILE "$@"
