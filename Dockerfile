FROM openjdk:8-jre-stretch

RUN apt-get update && apt-get -y install chromium

RUN useradd -Ums /bin/bash zrender
USER zrender

COPY target/pack /opt/zrender

CMD ["/opt/zrender/bin/zrender"]