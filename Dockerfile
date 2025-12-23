FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    wget ca-certificates gnupg \
    mesa-utils libgl1 libglvnd0 libglx0 libegl1 \
    fonts-liberation xdg-utils 

COPY docker/virtualgl_3.1.4_amd64.deb /tmp/virtualgl.deb
RUN apt-get update && apt-get install -y /tmp/virtualgl.deb \
    && rm -f /tmp/virtualgl.deb 

# Install Google Chrome (non-snap)
RUN wget -qO- https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor > /usr/share/keyrings/google-linux.gpg \
 && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
    > /etc/apt/sources.list.d/google-chrome.list \
 && apt-get update && apt-get install -y google-chrome-stable 
