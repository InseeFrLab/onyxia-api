FROM curlimages/curl as build  
RUN curl -o /tmp/kubectl -L https://storage.googleapis.com/kubernetes-release/release/v1.22.5/bin/linux/amd64/kubectl
RUN curl -o /tmp/helm -L https://get.helm.sh/helm-v3.7.2-linux-amd64.tar.gz
RUN tar -xvzf /tmp/helm -C /tmp

FROM eclipse-temurin:17-jre
COPY --from=build /tmp/kubectl /usr/local/bin/kubectl
COPY --from=build /tmp/linux-amd64/helm /usr/local/bin/helm
COPY target/*.jar app.jar
RUN chmod +x /usr/local/bin/kubectl && \
    chmod +x /usr/local/bin/helm && \
    groupadd --gid 101 --system onyxia && \
    useradd --system --uid 101 --create-home --home-dir /var/cache/onyxia --shell /sbin/nologin --gid onyxia --comment onyxia onyxia
# Equivalent to 'USER onyxia', see: https://github.com/InseeFrLab/onyxia-api/pull/116
USER 101
ENTRYPOINT ["java","-jar","/app.jar"]
