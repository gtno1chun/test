ARG REGISTRY_SERVER
FROM ${REGISTRY_SERVER}/library/eclipse-temurin:17-jre-focal
ARG BUILD_ENV
ENV BUILD_ENV=${BUILD_ENV}
ENV APP_HOME=/app \
    LANGUAGE=/\
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Asia/Seoul \
    JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=${BUILD_ENV}"
COPY target/*.jar ${APP_HOME}/app.jar
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone && \
    groupadd -r -g 1001 app && \
    useradd -u 1001 -r -g app app && \
    chown -R app:app ${APP_HOME}
USER app
WORKDIR ${APP_HOME}
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar ${APP_HOME}/app.jar"]
