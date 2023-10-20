 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.17

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ecm-consumer.jar /opt/app/

EXPOSE 8085
CMD [ "ecm-consumer.jar" ]
