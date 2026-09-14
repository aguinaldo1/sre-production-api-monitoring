# SLI, SLO and Error Budget — Introductory Extension

> This section is an introductory reliability engineering extension of the
> Production API Monitoring project.
>
> The main objective of this project remains monitoring, alerting and incident
> detection using Prometheus, Grafana and Alertmanager.
>
> A dedicated portfolio project will explore SLI, SLO, Error Budget and
> Burn Rate engineering in greater depth.

## Service Scope

The reliability objective considers only the user-facing API endpoints:

    /products
    /orders
    /payments

The following endpoints are excluded from the production-style reliability calculation:

    /actuator/*
    /health
    /slow
    /error-test

These endpoints are operational or synthetic and do not represent normal user traffic.

## Availability SLI

The Availability SLI measures the percentage of successful HTTP requests among the selected user-facing endpoints.

Formula:

    successful requests / total requests * 100

PromQL:

    100 *
    sum(
      increase(
        http_server_requests_seconds_count{
          job="reliability-api",
          uri=~"/products|/orders|/payments",
          status=~"2.."
        }[5m]
      )
    )
    /
    sum(
      increase(
        http_server_requests_seconds_count{
          job="reliability-api",
          uri=~"/products|/orders|/payments"
        }[5m]
      )
    )

Observed SLI during the baseline validation:

    Availability = 100%

## Availability SLO

Target:

    Availability >= 99.9%

The service should successfully serve at least 99.9% of valid user-facing requests.

## Error Budget

The error budget is calculated as:

    100% - 99.9% = 0.1%

This means that, for every 100,000 requests, up to approximately 100 failed requests can be tolerated before the error budget is exhausted.

## Time-Based Interpretation

For a 30-day period:

    30 days = 43,200 minutes

A 0.1% error budget corresponds to approximately:

    43.2 minutes

or approximately:

    43 minutes and 12 seconds

of unavailable service time.

## Important Note

The primary SLI demonstrated in this lab is request-based.

The time-based error budget is included only as an intuitive interpretation
of a 99.9% availability objective and should not be confused with the
request-based SLI calculation.

The short 5-minute recording rules used in the lab are operational examples
for validating the metrics pipeline. They do not by themselves represent a
complete 30-day SLO implementation.

## Project Boundary

The concepts introduced here provide a foundation for a future dedicated
Reliability Engineering project covering:

- longer SLO measurement windows;
- Error Budget consumption;
- Burn Rate analysis;
- multi-window Burn Rate alerting;
- reliability policy and operational response.

## Conclusion

The Production API Monitoring project demonstrates how application metrics
can evolve from basic infrastructure monitoring toward service-oriented
reliability indicators.

The deeper implementation of SLI, SLO and Error Budget engineering is
intentionally reserved for a dedicated portfolio project.
