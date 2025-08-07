FROM eclipse-temurin:21.0.8_9-jre AS extract
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar  extract

FROM eclipse-temurin:21.0.8_9-jre
WORKDIR /app
# Install helm
COPY install-helm.sh install-helm.sh
RUN ./install-helm.sh
RUN rm install-helm.sh
# Create onyxia group and user
RUN groupadd --gid 101 --system onyxia && \
    useradd --system --uid 101 --create-home --home-dir /var/cache/onyxia --shell /sbin/nologin --gid onyxia --comment onyxia onyxia
# Allow adding CA certificates
ENV CACERTS_DIR="/usr/local/share/ca-certificates"
RUN chown onyxia $JAVA_HOME/lib/security/cacerts
# copy build extraction
COPY --from=extract dependencies/ ./
COPY --from=extract snapshot-dependencies/ ./
COPY --from=extract spring-boot-loader/ ./
COPY --from=extract application/ ./
COPY entrypoint.sh entrypoint.sh
# Equivalent to 'USER onyxia', see: https://github.com/InseeFrLab/onyxia-api/pull/116
USER 101
ENTRYPOINT ["/app/entrypoint.sh"]
