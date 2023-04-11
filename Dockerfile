ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:11-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ecm-consumer.jar /opt/app/

EXPOSE 8085
CMD [ "ecm-consumer.jar" ]
