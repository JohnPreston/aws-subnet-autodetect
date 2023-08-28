# Sample Dockerfile to build and test the lib as a service.
ARG JAVA_VERSION=17
ARG BASE_IMAGE=public.ecr.aws/amazoncorretto/amazoncorretto:${JAVA_VERSION}
FROM $BASE_IMAGE

FROM public.ecr.aws/ews-network/aws-rds-ca as certbuild


FROM $BASE_IMAGE as certimport
ADD https://s3.amazonaws.com/rds-downloads/rds-combined-ca-bundle.pem /etc/ssl/certs/rds-combined-ca-bundle.pem
ADD https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem /etc/ssl/certs/aws-global.pem
COPY --from=certbuild /var/opt/aws-rds.jks /var/opt/aws-rds.jks

RUN keytool -importkeystore -srckeystore /var/opt/aws-rds.jks -cacerts -srcstorepass changeit -deststorepass changeit

FROM certimport
RUN yum upgrade --security -y --exclude=kernel*;                                \
    yum install -y shadow-utils ;                                               \
    groupadd -r app -g 1042 &&                                                  \
    useradd -u 1042 -r -g app -m -d /app -s /sbin/nologin -c "App user" app &&  \
    chown -R app: /app &&                                                       \
    yum erase shadow-utils -y && yum clean all && rm -rfv /var/cache/yum

ARG JAR_FILE
ENV JAR_FILE $JAR_FILE
COPY --chown=app:app $JAR_FILE /app/app.jar
COPY --chown=app:app entrypoint.sh /app/entrypoint.sh
RUN chmod 755 /app/entrypoint.sh; chmod 644 /app/app.jar
ENV JAVA_DEFAULT_OPTS="-Xms128M"
WORKDIR /app
USER app
ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["-jar", "/app/app.jar"]
