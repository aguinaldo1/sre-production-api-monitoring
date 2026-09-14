# Incident Simulation — High HTTP Latency

## Objective

Validate detection and recovery of elevated HTTP latency in the Reliability API using Prometheus histogram metrics and Alertmanager.

## Synthetic Latency

The application contains a controlled latency endpoint:

    GET /slow

The endpoint intentionally waits approximately 1000 ms before returning HTTP 200.

Observed response time:

    HTTP 200
    total approximately 1.013 seconds

## Alert

Alert name: HighLatency

Conditions:

- HTTP request p95 latency greater than 500 ms
- At least 20 requests during the 5-minute window
- Condition must remain active for 1 minute
- Severity: warning

## Failure Simulation

Slow traffic was generated against the synthetic endpoint:

    for i in {1..25}; do
      curl -s http://localhost:8080/slow > /dev/null
    done

Additional requests were generated to maintain the degraded condition:

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

The slow endpoint continued returning HTTP 200.

This demonstrates that an application can be technically available while still providing degraded user experience.

Availability alone is therefore insufficient to evaluate service reliability.

## Recovery

Slow traffic was stopped and healthy traffic continued.

As the slow requests aged out of the 5-minute Prometheus evaluation window, the latency condition no longer satisfied the alert expression.

Prometheus automatically resolved the alert and Alertmanager removed the active incident.

## Conclusion

The monitoring stack successfully detected latency degradation using HTTP histogram metrics and p95 latency, triggered the configured alert, propagated it to Alertmanager, and automatically recovered after service performance returned to normal.
