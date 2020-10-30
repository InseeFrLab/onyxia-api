FROM curlimages/curl as build  
RUN curl -o /tmp/kubectl -L https://storage.googleapis.com/kubernetes-release/release/v1.18.9/bin/linux/amd64/kubectl
RUN curl -o /tmp/helm -L https://get.helm.sh/helm-v3.4.0-linux-amd64.tar.gz
RUN tar -xvzf /tmp/helm -C /tmp

FROM openjdk:11-jre-slim
COPY --from=build /tmp/kubectl /usr/local/bin/kubectl
RUN chmod +x /usr/local/bin/kubectl
COPY --from=build /tmp/linux-amd64/helm /usr/local/bin/helm
RUN chmod +x /usr/local/bin/helm
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
