FROM eclipse-temurin:21_35-jre
# Install helm
RUN curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | DESIRED_VERSION=v3.12.0 bash
COPY target/*.jar app.jar
RUN groupadd --gid 101 --system onyxia && \
    useradd --system --uid 101 --create-home --home-dir /var/cache/onyxia --shell /sbin/nologin --gid onyxia --comment onyxia onyxia
# Allow adding CA certificates
ENV CACERTS_DIR="/usr/local/share/ca-certificates"
RUN chown onyxia $JAVA_HOME/lib/security/cacerts
COPY entrypoint.sh entrypoint.sh
# Equivalent to 'USER onyxia', see: https://github.com/InseeFrLab/onyxia-api/pull/116
USER 101
ENTRYPOINT ["/entrypoint.sh"]
