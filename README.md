# SRE Production API Monitoring

Projeto prático de **Site Reliability Engineering (SRE)** focado em monitoramento de aplicações, observabilidade, detecção de falhas e resposta a incidentes.

O laboratório utiliza uma API desenvolvida em **Java com Spring Boot**, instrumentada com **Spring Boot Actuator e Micrometer** e monitorada por **Prometheus, Grafana e Alertmanager**.

---

## 📌 Problema de produção

Uma aplicação pode permanecer tecnicamente disponível enquanto apresenta degradações que afetam diretamente seus usuários.

Entre os principais sintomas estão:

- aumento da latência;
- crescimento da taxa de erros;
- indisponibilidade completa;
- comportamento anormal da aplicação;
- degradação da experiência do usuário.

O objetivo deste projeto foi construir um ambiente controlado onde esses problemas pudessem ser simulados, observados e diagnosticados utilizando métricas e alertas.

---

## 🎯 Objetivos do projeto

O laboratório demonstra:

- instrumentação de uma aplicação Java;
- exposição de métricas com Spring Boot Actuator e Micrometer;
- coleta de métricas utilizando Prometheus;
- criação de dashboards no Grafana;
- configuração de alertas;
- integração com Alertmanager;
- análise dos Golden Signals;
- simulação controlada de incidentes;
- investigação baseada em métricas;
- recuperação do serviço;
- documentação de evidências;
- introdução aos conceitos de SLI, SLO e Error Budget.

---

## 🏗️ Arquitetura

```text
                    +----------------------+
                    |       Cliente        |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |   Reliability API    |
                    | Java / Spring Boot   |
                    +----------+-----------+
                               |
          +--------------------+--------------------+
          |                    |                    |
          v                    v                    v
     /products             /orders             /payments

                               |
                               v
                      Spring Boot Actuator
                               |
                               v
                           Micrometer
                               |
                               v
                     /actuator/prometheus
                               |
                               v
                         +------------+
                         | Prometheus |
                         +-----+------+
                               |
                     +---------+---------+
                     |                   |
                     v                   v
                +---------+        +-------------+
                | Grafana |        | Alertmanager|
                +---------+        +-------------+
```

---

## 🛠️ Stack utilizada

| Tecnologia | Finalidade |
|---|---|
| Java 17 | Desenvolvimento da API |
| Spring Boot | Framework da aplicação |
| Spring Boot Actuator | Endpoints operacionais e métricas |
| Micrometer | Instrumentação da aplicação |
| Prometheus | Coleta e consulta de métricas |
| PromQL | Consultas e regras de monitoramento |
| Grafana | Visualização das métricas |
| Alertmanager | Gerenciamento dos alertas |
| Docker | Execução dos componentes de monitoramento |
| Docker Compose | Orquestração local |
| Linux / WSL2 | Ambiente operacional |
| Git | Controle de versão |
| GitHub | Versionamento e apresentação do projeto |

---

## 🌐 API monitorada

### Endpoints funcionais

Os principais endpoints que representam tráfego de usuário são:

```text
GET /products
GET /orders
GET /payments
```

### Endpoints operacionais e de laboratório

```text
GET /health
GET /slow
GET /error-test
GET /actuator/health
GET /actuator/prometheus
```

Os endpoints `/slow` e `/error-test` foram criados especificamente para permitir simulações controladas de degradação e falhas durante o laboratório.

---

## 📊 Golden Signals

O projeto trabalha principalmente com três sinais fundamentais para acompanhamento da confiabilidade da aplicação.

### Traffic

Representa o volume de requisições HTTP processadas pela aplicação.

### Errors

Representa requisições que resultam em falhas HTTP.

### Latency

Representa o tempo necessário para responder às requisições.

O projeto utiliza especialmente o percentil **p95** para identificar degradações que poderiam ficar escondidas em uma média simples.

### Availability do target

A disponibilidade do target monitorado pelo Prometheus é observada através de:

```promql
up{job="reliability-api"}
```

Quando a aplicação está acessível para coleta:

```text
up = 1
```

Quando o Prometheus não consegue coletar suas métricas:

```text
up = 0
```

Essa métrica representa a disponibilidade do **target para o Prometheus** e não deve ser confundida com um SLI completo de disponibilidade do serviço para os usuários.

---

## 📈 Prometheus

O Prometheus coleta métricas da aplicação através de:

```text
/actuator/prometheus
```

O target utilizado no laboratório é identificado pelo job:

```text
reliability-api
```

Configuração principal:

```text
monitoring/prometheus/prometheus.yml
```

Regras de alertas:

```text
monitoring/prometheus/rules/reliability-api-alerts.yml
```

Recording rules introdutórias:

```text
monitoring/prometheus/rules/reliability-api-sli.rules.yml
```

---

## 📉 Grafana

O Grafana é utilizado como camada de visualização das métricas coletadas pelo Prometheus.

O dashboard principal apresenta:

- disponibilidade do target;
- taxa de requisições HTTP;
- taxa de erros HTTP;
- latência HTTP p95.

Dashboard versionado:

```text
monitoring/grafana/dashboards/reliability-api-sre-overview.json
```

Evidência visual:

```text
evidence/dashboards/reliability-api-sre-overview.png
```

---

## 🚨 Alertas implementados

### ReliabilityApiDown

Detecta quando o Prometheus não consegue coletar métricas da Reliability API por pelo menos 1 minuto.

```promql
up{job="reliability-api"} == 0
```

Severidade:

```text
critical
```

### HighErrorRate

Detecta taxa elevada de erros HTTP com volume mínimo de tráfego para reduzir falsos positivos.

Condição utilizada no laboratório:

```text
Error Rate > 10%
Requests >= 20 em 5 minutos
```

Severidade:

```text
warning
```

### HighLatency

Detecta degradação da latência utilizando o percentil p95.

Condição utilizada no laboratório:

```text
p95 > 500 ms
Requests >= 20 em 5 minutos
```

Severidade:

```text
warning
```

Os thresholds foram definidos para fins didáticos e reprodução dos cenários dentro do laboratório. Eles não representam valores universais para ambientes de produção.

---

# 🔥 Incidentes simulados

O projeto incluiu três cenários controlados de incidente.

```text
Baseline
   |
   v
Falha / Degradação
   |
   v
Coleta de métricas
   |
   v
Detecção
   |
   v
Alerta
   |
   v
Investigação
   |
   v
Recuperação
   |
   v
Evidência
```

---

## Incidente 1 — High Error Rate

Foi gerado tráfego contendo respostas HTTP 500 para simular aumento da taxa de erros.

```text
Tráfego
   |
   v
Erros HTTP
   |
   v
Prometheus
   |
   v
HighErrorRate
   |
   v
Alertmanager
   |
   v
Recuperação
```

Durante o experimento foi possível acompanhar o alerta até o estado:

```text
firing
```

e posteriormente sua recuperação.

Documentação:

```text
docs/high-error-rate-incident.md
```

---

## Incidente 2 — Reliability API Down

O processo Java da aplicação foi interrompido para simular indisponibilidade completa.

O Prometheus passou a observar:

```text
up = 0
```

O alerta:

```text
ReliabilityApiDown
```

entrou em estado `firing` e foi encaminhado ao Alertmanager.

Após o serviço ser reiniciado:

```text
up = 1
```

e o alerta foi automaticamente resolvido.

Documentação:

```text
docs/api-down-incident.md
```

---

## Incidente 3 — High Latency

O endpoint:

```text
/slow
```

foi utilizado para introduzir aproximadamente **1 segundo de latência** nas requisições.

Mesmo durante a degradação, as requisições continuavam retornando:

```text
HTTP 200
```

Porém, o p95 ultrapassou o threshold definido no laboratório e o alerta:

```text
HighLatency
```

foi ativado.

Esse experimento demonstra um princípio importante de SRE:

> Uma aplicação responder HTTP 200 não significa necessariamente que ela esteja oferecendo uma boa experiência ao usuário.

Documentação:

```text
docs/high-latency-incident.md
```

---

## 🔎 Evidências

As evidências coletadas durante os experimentos foram preservadas no próprio repositório.

```text
evidence/
├── alerts/
├── dashboards/
└── latency/
```

Entre as evidências disponíveis estão:

- alertas em estado `firing`;
- alertas recebidos pelo Alertmanager;
- recuperação dos alertas;
- disponibilidade da Reliability API;
- p95 em condição baseline;
- p95 durante degradação;
- dashboard operacional do Grafana.

---

# 🎯 SLI, SLO e Error Budget — extensão introdutória

Como extensão do laboratório de monitoramento, foi criada uma primeira camada de indicadores de confiabilidade orientados ao serviço.

Essa parte não representa uma implementação completa de SLO management.

Seu objetivo é estabelecer uma fundação para um projeto posterior dedicado especificamente a **Reliability Engineering**.

## Service Scope

Somente endpoints funcionais são considerados:

```text
/products
/orders
/payments
```

São excluídos:

```text
/actuator/*
/health
/slow
/error-test
```

Isso evita que tráfego operacional ou sintético distorça o indicador destinado a representar a experiência dos usuários.

## Availability SLI

O SLI introdutório mede a proporção de requisições bem-sucedidas nos endpoints funcionais.

```text
successful requests
------------------- × 100
 total requests
```

Objetivo utilizado como exemplo:

```text
SLO = 99.9%
```

Consequentemente:

```text
Error Budget = 0.1%
```

Em uma interpretação baseada em requisições:

```text
100.000 requisições
        |
        v
99.900 sucessos necessários
        |
        v
até aproximadamente 100 falhas dentro do budget
```

## Recording Rules

Foram implementadas recording rules para:

```text
Availability
Error Ratio
Request Rate
Error Budget Burn Rate
```

As métricas gravadas incluem:

```promql
reliability_api:availability:ratio_5m
```

```promql
reliability_api:error_ratio:5m
```

```promql
reliability_api:request_rate:5m
```

```promql
reliability_api:error_budget_burn_rate:5m
```

A recording rule de Burn Rate utiliza como referência:

```text
Allowed Error Ratio = 0.001
```

Conceitualmente:

```text
Burn Rate = Observed Error Ratio / Allowed Error Ratio
```

## Limite desta implementação

As recording rules de **5 minutos** utilizadas neste projeto são exemplos operacionais para validação da pipeline de métricas.

Elas, isoladamente, **não constituem uma implementação completa de um SLO de 30 dias**.

Conceitos mais avançados, como:

- janelas longas de SLO;
- consumo acumulado de Error Budget;
- Burn Rate operacional;
- multi-window Burn Rate;
- alertas baseados em Error Budget;
- políticas de confiabilidade;

serão tratados em um projeto posterior dedicado especificamente a SLI, SLO e Error Budget.

Documentação:

```text
docs/sli-slo.md
```

---

# 📁 Estrutura do projeto

```text
.
├── app/
│   └── aplicação Java Spring Boot
│
├── monitoring/
│   ├── alertmanager/
│   ├── grafana/
│   ├── prometheus/
│   └── docker-compose.yml
│
├── docs/
│   ├── api-down-incident.md
│   ├── high-error-rate-incident.md
│   ├── high-latency-incident.md
│   └── sli-slo.md
│
├── evidence/
│   ├── alerts/
│   ├── dashboards/
│   └── latency/
│
└── README.md
```

---

# 🧠 Principais aprendizados

Este laboratório permitiu praticar:

- monitoramento orientado ao comportamento da aplicação;
- instrumentação de aplicações Java;
- Spring Boot Actuator e Micrometer;
- coleta de métricas com Prometheus;
- consultas PromQL;
- visualização operacional com Grafana;
- gerenciamento de alertas com Alertmanager;
- diferença entre disponibilidade e desempenho;
- análise de métricas HTTP;
- percentis para análise de latência;
- alertas com proteção contra baixo volume;
- separação entre tráfego funcional e operacional;
- investigação baseada em evidências;
- simulação controlada de incidentes;
- ciclo de detecção e recuperação;
- introdução a SLI, SLO e Error Budget.

---

# 🏁 Resultado

Ao final do projeto foi construída uma aplicação Java monitorada de ponta a ponta:

```text
Java / Spring Boot
        |
        v
Actuator + Micrometer
        |
        v
      Metrics
        |
        v
    Prometheus
        |
        +----------------+
        |                |
        v                v
     Grafana          Alert Rules
                         |
                         v
                    Alertmanager
```

O laboratório demonstra o ciclo operacional:

```text
Baseline
   |
   v
Degradação
   |
   v
Detecção
   |
   v
Alerta
   |
   v
Investigação
   |
   v
Recuperação
   |
   v
Evidência
```

O resultado é um laboratório reproduzível que demonstra competências práticas em:

**SRE · Java · Linux · Prometheus · Grafana · Alertmanager · PromQL · Observabilidade · Monitoramento · Troubleshooting · Incident Response**

---

# ✅ Status do projeto

```text
Projeto 2 — Production API Monitoring

STATUS: COMPLETO
```

Este projeto faz parte de um **portfólio progressivo de SRE e DevOps**, no qual cada laboratório adiciona uma nova camada de complexidade e competência operacional.

A próxima evolução do portfólio será dedicada a **Full Stack Observability**, expandindo a visão baseada em métricas para correlação entre **métricas, logs e traces**.
