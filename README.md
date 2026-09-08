# SRE Production API Monitoring

Projeto prático de Site Reliability Engineering focado em monitoramento de aplicações, observabilidade, detecção de falhas e resposta a incidentes.

## Problema de produção

Uma API em produção pode continuar disponível mesmo apresentando degradação de desempenho.

Aumento de latência, crescimento da taxa de erros ou comportamento anormal podem afetar usuários antes mesmo que a aplicação fique completamente indisponível.

O objetivo deste projeto é construir um ambiente controlado onde esses problemas possam ser simulados, detectados e investigados através de métricas e alertas.

## Impacto no negócio

Falhas de desempenho em uma API podem causar:

- aumento no tempo de resposta;
- erros em requisições;
- abandono de operações pelos usuários;
- degradação da experiência do cliente;
- aumento do tempo de detecção de incidentes;
- impacto em serviços dependentes da API.

## Objetivo técnico

Construir uma API monitorada utilizando práticas de SRE e observabilidade.

O projeto deverá demonstrar:

- exposição de métricas da aplicação;
- coleta de métricas com Prometheus;
- visualização através do Grafana;
- criação de alertas;
- detecção de aumento de latência;
- detecção de erros HTTP;
- investigação baseada em evidências;
- mitigação do incidente;
- documentação do diagnóstico;
- melhoria contínua da confiabilidade.

## Arquitetura planejada

```text
Cliente
   |
   v
Sample API
   |
   +--> /health
   +--> /products
   +--> /orders
   +--> /payments
   |
   v
/metrics
   |
   v
Prometheus
   |
   +--> Grafana
   |
   +--> Alertmanager
