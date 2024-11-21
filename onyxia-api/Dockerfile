FROM eclipse-temurin:21.0.4_7-jre AS extract
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar  extract

FROM eclipse-temurin:21.0.4_7-jre
WORKDIR /app
# Install helm
RUN curl -vI https://get.helm.sh/helm-v3.16.1-linux-arm64.tar.gz
RUN curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | DESIRED_VERSION=v3.16.1 bash
RUN groupadd --gid 101 --system onyxia && \
    useradd --system --uid 101 --create-home --home-dir /var/cache/onyxia --shell /sbin/nologin --gid onyxia --comment onyxia onyxia
# Allow adding CA certificates
ENV CACERTS_DIR="/usr/local/share/ca-certificates"
RUN chown onyxia $JAVA_HOME/lib/security/cacerts
COPY --from=extract dependencies/ ./
COPY --from=extract snapshot-dependencies/ ./
COPY --from=extract spring-boot-loader/ ./
COPY --from=extract application/ ./
COPY entrypoint.sh entrypoint.sh
# Equivalent to 'USER onyxia', see: https://github.com/InseeFrLab/onyxia-api/pull/116
USER 101
ENTRYPOINT ["/app/entrypoint.sh"]
