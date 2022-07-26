FROM eclipse-temurin:17-jre
# Install kubectl
RUN apt-get update && apt-get install -y apt-transport-https gnupg2
RUN curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -
RUN echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | tee -a /etc/apt/sources.list.d/kubernetes.list
RUN apt-get update && apt-get install -y kubectl
# Install helm
RUN curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
COPY target/*.jar app.jar
RUN groupadd --gid 101 --system onyxia && \
    useradd --system --uid 101 --create-home --home-dir /var/cache/onyxia --shell /sbin/nologin --gid onyxia --comment onyxia onyxia
# Equivalent to 'USER onyxia', see: https://github.com/InseeFrLab/onyxia-api/pull/116
USER 101
ENTRYPOINT ["java","-jar","/app.jar"]
