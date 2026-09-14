# Incident Simulation — High HTTP Error Rate

## Objective

Validate the end-to-end alerting flow for the Reliability API using Prometheus and Alertmanager.

## Alert

Alert name: HighErrorRate

Conditions:

- HTTP 5xx error rate greater than 10%
- At least 20 HTTP requests during the 5-minute evaluation window
- Condition must remain active for 1 minute

## Traffic Simulation

Healthy traffic:

    for i in {1..80}; do
      curl -s http://localhost:8080/health > /dev/null
    done

Failure traffic:

    for i in {1..20}; do
      curl -s http://localhost:8080/error-test > /dev/null
    done

## Observed Results

During the incident:

- HTTP error rate reached approximately 20%
- Request volume exceeded the minimum threshold
- Prometheus changed HighErrorRate to FIRING
- Alert health remained ok
- Alertmanager received the alert
- Severity was warning

Alert pipeline validated:

Application -> Prometheus -> Alert Rule -> Alertmanager

## Recovery

After the failure injection stopped, healthy traffic was generated:

    for i in {1..200}; do
      curl -s http://localhost:8080/health > /dev/null
    done

Prometheus returned no active alerts:

    {"status":"success","data":{"alerts":[]}}

Alertmanager also returned no active alerts:

    []

## Conclusion

The monitoring stack successfully detected an elevated HTTP 5xx error rate, triggered the configured alert, propagated it to Alertmanager, and automatically resolved the alert after the service returned to healthy behavior.
