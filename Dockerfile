ARG APP_INSIGHTS_AGENT_VERSION=3.5.1
FROM hmctspublic.azurecr.io/base/java:21-distroless as base

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ecm-consumer.jar /opt/app/

EXPOSE 8085
CMD [ "ecm-consumer.jar" ]
