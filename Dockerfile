FROM ubuntu:latest
LABEL authors="brynjulv"

ENTRYPOINT ["top", "-b"]