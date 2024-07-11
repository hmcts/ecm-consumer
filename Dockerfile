FROM hmctspublic.azurecr.io/base/java:17-distroless as base
ARG APP_INSIGHTS_AGENT_VERSION=3.4.11
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ecm-consumer.jar /opt/app/

EXPOSE 8085
CMD [ "ecm-consumer.jar" ]
