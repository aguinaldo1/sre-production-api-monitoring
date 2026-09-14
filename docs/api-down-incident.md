# Incident Simulation — API Unavailable

## Objective

Validate the end-to-end detection and recovery flow for complete unavailability of the Reliability API.

## Alert

Alert name: ReliabilityApiDown

Condition:

- Prometheus target availability equal to zero
- Expression: up{job="reliability-api"} == 0
- Condition must remain active for 1 minute
- Severity: critical

## Failure Simulation

The Spring Boot application process was terminated intentionally.

After the application stopped:

- HTTP health endpoint became unavailable
- curl returned HTTP code 000
- Prometheus changed the target metric from up=1 to up=0

## Detection

Prometheus detected the service outage and changed the ReliabilityApiDown alert to FIRING.

Observed state:

- Alert: ReliabilityApiDown
- State: firing
- Severity: critical
- Service: reliability-api

Alertmanager received the alert and marked it as active.

Detection pipeline:

Application Down -> Prometheus Scrape Failure -> up=0 -> Alert Rule -> Alertmanager

## Recovery

The application was restarted using Maven:

    nohup ./mvnw spring-boot:run > /tmp/reliability-api.log 2>&1 &

The health endpoint returned:

    {"status":"UP","service":"reliability-api"}

Prometheus confirmed recovery:

    up{job="reliability-api"} = 1

Prometheus active alerts returned:

    {"status":"success","data":{"alerts":[]}}

Alertmanager returned:

    []

## Conclusion

The monitoring stack successfully detected complete application unavailability, triggered a critical alert, propagated it to Alertmanager, and automatically resolved the incident after the service recovered.
