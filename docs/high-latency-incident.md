# Incident Simulation — High HTTP Latency

## Objective

Validate detection and recovery of elevated HTTP latency in the Reliability API using Prometheus histogram metrics and Alertmanager.

## Synthetic Latency Endpoint

The application provides a controlled latency endpoint:

    GET /slow

The endpoint intentionally waits approximately 1000 ms before returning HTTP 200.

Observed response:

    HTTP 200
    total approximately 1.013 seconds

## Alert

Alert name: HighLatency

Conditions:

- HTTP request p95 latency greater than 500 ms
- At least 20 HTTP requests in the 5-minute evaluation window
- Condition must remain active for 1 minute
- Severity: warning

## Failure Simulation

Slow traffic was generated:

    for i in {1..25}; do
      curl -s http://localhost:8080/slow > /dev/null
    done

Additional slow traffic was generated to keep the condition active:

    for i in {1..20}; do
      curl -s http://localhost:8080/slow > /dev/null
    done

## Observed Results

Prometheus measured approximately:

    p95 latency = 1.069 seconds

Request volume reached approximately:

    48 requests in the 5-minute evaluation window

The alert transitioned through:

    inactive -> pending -> firing

Prometheus reported:

    HighLatency
    severity = warning
    state = firing

Alertmanager received the alert and marked it as active.

## Important Observation

The endpoint continued returning HTTP 200 while latency exceeded the configured threshold.

This demonstrates that a service can remain technically available while still providing degraded user experience.

Availability alone is not sufficient to evaluate service reliability.

## Recovery

Slow traffic was stopped and only healthy traffic was generated.

During recovery, p95 still remained around:

    1.02 seconds

However, as traffic aged out of the 5-minute window, the minimum request-volume condition was no longer satisfied.

The alert condition became false.

Prometheus returned:

    {"status":"success","data":{"alerts":[]}}

Alertmanager later returned:

    []

## Conclusion

The monitoring stack successfully detected latency degradation using HTTP histogram metrics and p95 latency, triggered the HighLatency alert, propagated it to Alertmanager, and automatically resolved the alert after the alert conditions were no longer satisfied.
