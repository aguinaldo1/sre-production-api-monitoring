# Evidências de Alertas

Este diretório reúne evidências técnicas dos cenários de monitoramento e alertas implementados no projeto **SRE Production API Monitoring**.

Os testes foram construídos para demonstrar dois tipos diferentes de falha operacional:

```text
ReliabilityApiDown
        ↓
Target unavailable

HighErrorRate
        ↓
Target available, service degraded
```

Essa distinção é importante porque um serviço pode continuar acessível e, ainda assim, apresentar degradação significativa para seus usuários.

---

# ReliabilityApiDown

## Objetivo

O alerta `ReliabilityApiDown` foi validado por meio de um ciclo completo de falha, detecção e recuperação da aplicação.

O objetivo deste teste foi demonstrar, na prática, como a camada de monitoramento identifica a indisponibilidade da Reliability API, aciona um alerta e reconhece posteriormente a recuperação do serviço.

## Cenário de Falha

A Reliability API foi interrompida intencionalmente para simular uma indisponibilidade de serviço em um ambiente de produção.

Durante a interrupção, o Prometheus deixou de conseguir realizar o `scrape` das métricas da aplicação.

A métrica de disponibilidade do `target` passou a apresentar:

```promql
up{job="reliability-api"} = 0
```

A regra utilizada para detectar essa condição foi:

```promql
up{job="reliability-api"} == 0
```

A regra foi configurada com:

```text
for: 1m
```

Isso significa que a condição precisava permanecer verdadeira durante pelo menos 1 minuto antes de o alerta ser considerado ativo.

Após esse período, o alerta:

```text
ReliabilityApiDown
```

entrou no estado:

```text
state = firing
```

O Alertmanager também recebeu o alerta com os seguintes `labels`:

```text
alertname = ReliabilityApiDown
service   = reliability-api
severity  = critical
```

## Detection Flow

O fluxo técnico validado durante a simulação foi:

```text
Reliability API unavailable
        ↓
Prometheus scrape fails
        ↓
up{job="reliability-api"} = 0
        ↓
ReliabilityApiDown condition becomes true
        ↓
Condition remains true for 1 minute
        ↓
Alert state = firing
        ↓
Alertmanager receives ReliabilityApiDown
```

Esse fluxo demonstra a cadeia básica de detecção de indisponibilidade implementada no projeto.

## Recuperação do Serviço

Após a validação do alerta, a Reliability API foi iniciada novamente.

O primeiro passo da validação da recuperação foi verificar o endpoint de saúde da aplicação:

```text
/actuator/health
```

A aplicação voltou a responder:

```text
HTTP 200
```

Em seguida, o Prometheus voltou a realizar o `scrape` das métricas da aplicação com sucesso.

A métrica `up` retornou para:

```promql
up{job="reliability-api"} = 1
```

Com o `target` novamente disponível, a condição:

```promql
up{job="reliability-api"} == 0
```

deixou de ser verdadeira.

O alerta `ReliabilityApiDown` retornou então para:

```text
state = inactive
```

A regra no Prometheus também apresentou:

```text
health = ok
alerts = []
```

## Recovery Flow

O fluxo técnico de recuperação validado foi:

```text
Reliability API restored
        ↓
/actuator/health = HTTP 200
        ↓
Prometheus scrape succeeds
        ↓
up{job="reliability-api"} = 1
        ↓
ReliabilityApiDown condition becomes false
        ↓
Alert state = inactive
```

## Evidências Persistidas

### Alert Recovery

Arquivo:

```text
reliability-api-down-recovered.txt
```

Esse arquivo registra o estado da regra `ReliabilityApiDown` após a recuperação da aplicação.

Entre as informações registradas estão:

```text
state  = inactive
health = ok
alerts = []
```

Essa evidência demonstra que, após a recuperação do serviço, não existiam mais instâncias ativas do alerta.

### Prometheus Target Recovery

Arquivo:

```text
reliability-api-target-up.txt
```

Esse arquivo registra o resultado da consulta da métrica `up` após a recuperação da aplicação.

O Prometheus apresentou:

```text
job      = reliability-api
instance = 172.17.0.1:8080
up       = 1
```

Isso demonstra que o Prometheus voltou a realizar o `scrape` da Reliability API com sucesso.

## Limitação das Evidências

Durante a simulação do incidente, o estado `firing` do alerta foi observado diretamente no Prometheus.

Também foi validado que o Alertmanager recebeu o alerta `ReliabilityApiDown`.

Entretanto, as saídas desses dois momentos não foram persistidas em arquivos durante a execução do teste.

Por esse motivo, este repositório não apresenta essas saídas como evidências persistidas.

As evidências armazenadas comprovam diretamente a etapa de recuperação:

```text
Application health = HTTP 200
Prometheus target  = UP
Alert state        = inactive
```

Essa distinção é mantida propositalmente para que a documentação represente somente evidências que possam ser verificadas no repositório.

## Resultado

O teste permitiu validar operacionalmente o seguinte ciclo:

```text
SERVICE FAILURE
      ↓
DETECTION
      ↓
ALERT FIRING
      ↓
ALERTMANAGER
      ↓
SERVICE RECOVERY
      ↓
PROMETHEUS TARGET UP
      ↓
ALERT INACTIVE
```

Com esse cenário, o projeto demonstra que a camada de monitoramento é capaz de:

- detectar a indisponibilidade da Reliability API;
- identificar a falha de `scrape` por meio da métrica `up`;
- avaliar uma regra de alerta;
- alterar o estado do alerta para `firing`;
- encaminhar o alerta para o Alertmanager;
- detectar a recuperação do serviço;
- retornar o alerta para `inactive`;
- preservar evidências técnicas da recuperação.

---

# HighErrorRate

## Objetivo

O alerta `HighErrorRate` foi implementado para detectar um cenário diferente de indisponibilidade total.

Nesse cenário, a aplicação permanece disponível para o Prometheus, porém apresenta degradação por meio de respostas HTTP `5xx`.

O objetivo foi demonstrar que:

```text
availability != reliability
```

Em outras palavras, o fato de um `target` continuar disponível para coleta de métricas não significa necessariamente que o serviço esteja funcionando adequadamente para seus usuários.

## Cenário de Degradação

Durante a simulação, a Reliability API permaneceu disponível:

```promql
up{job="reliability-api"} = 1
```

Ao mesmo tempo, foi utilizado um endpoint controlado:

```text
/error-test
```

Esse endpoint retorna intencionalmente:

```text
HTTP 500 Internal Server Error
```

A métrica correspondente foi observada com:

```text
outcome = SERVER_ERROR
status  = 500
uri     = /error-test
```

Isso permitiu gerar respostas HTTP `5xx` sem interromper completamente a aplicação.

O cenário simulado foi:

```text
Application available
        ↓
Normal HTTP 200 requests
        +
Controlled HTTP 500 requests
        ↓
Elevated HTTP Error Rate
```

---

## Primeira Versão da Detecção

A primeira versão da consulta utilizava `rate()`:

```promql
100 *
sum(
  rate(
    http_server_requests_seconds_count{
      job="reliability-api",
      status=~"5..",
      uri!~"/actuator.*"
    }[5m]
  )
)
/
sum(
  rate(
    http_server_requests_seconds_count{
      job="reliability-api",
      uri!~"/actuator.*"
    }[5m]
  )
)
```

Durante os testes, foi possível obter uma taxa de erro controlada de aproximadamente:

```text
16.67%
```

com o `threshold` definido em:

```text
10%
```

A condição foi então validada como verdadeira.

---

## Comportamento sem Tráfego

Durante a investigação da consulta, foi identificado um comportamento importante.

Quando não havia tráfego suficiente na janela observada, foram obtidos:

```text
5xx request rate   = 0
total request rate = 0
```

Consequentemente:

```text
0 / 0 = NaN
```

Esse resultado não representava uma falha do Prometheus nem da aplicação.

Ele demonstrava que não havia volume de tráfego suficiente naquele momento para calcular uma taxa percentual significativa.

Essa observação levou à evolução da regra.

---

## Risco de Baixo Volume

Também foi identificado outro problema potencial.

Considere o cenário:

```text
1 request
1 error
```

Matematicamente:

```text
Error Rate = 100%
```

Porém, uma única requisição pode não representar volume suficiente para justificar um alerta operacional.

Por isso, utilizar apenas:

```text
Error Rate > 10%
```

poderia produzir um sinal pouco representativo em períodos de tráfego muito baixo.

---

## Evolução da Regra

A regra foi evoluída para considerar duas condições simultaneamente:

```text
HTTP 5xx Error Rate > 10%
AND
Requests in the last 5 minutes >= 20
```

A expressão final utiliza `increase()` para observar quantas requisições ocorreram dentro da janela de 5 minutos.

A lógica utilizada é:

```promql
(
  100 *
  sum(
    increase(
      http_server_requests_seconds_count{
        job="reliability-api",
        status=~"5..",
        uri!~"/actuator.*"
      }[5m]
    )
  )
  /
  sum(
    increase(
      http_server_requests_seconds_count{
        job="reliability-api",
        uri!~"/actuator.*"
      }[5m]
    )
  )
) > 10
and
(
  sum(
    increase(
      http_server_requests_seconds_count{
        job="reliability-api",
        uri!~"/actuator.*"
      }[5m]
    )
  ) >= 20
)
```

Essa abordagem adiciona uma proteção contra alertas baseados em amostras muito pequenas.

---

## Validação da Nova Expressão

Primeiro, a expressão foi testada sem volume suficiente.

O Prometheus retornou:

```text
result = []
```

Isso significa que a condição do alerta não estava satisfeita.

Esse comportamento era esperado.

Depois foi gerado tráfego controlado composto aproximadamente por:

```text
40 HTTP 200 requests
10 HTTP 500 requests
```

Total:

```text
50 requests
```

Error Rate aproximado:

```text
20%
```

Nesse cenário:

```text
Error Rate > 10%      = TRUE
Requests >= 20        = TRUE
```

A consulta retornou:

```text
20
```

confirmando que a condição estava satisfeita.

---

## Configuração do Alerta

O alerta foi configurado como:

```text
alert    = HighErrorRate
severity = warning
service  = reliability-api
for      = 1m
```

O uso de:

```text
for: 1m
```

significa que a condição precisa permanecer verdadeira durante pelo menos 1 minuto antes de o alerta entrar no estado `firing`.

---

## Detection Flow

O fluxo técnico implementado é:

```text
Reliability API remains available
        ↓
up{job="reliability-api"} = 1
        ↓
Controlled HTTP 500 responses
        ↓
HTTP 5xx Error Rate > 10%
        ↓
Request volume >= 20 in 5m
        ↓
Condition remains true for 1 minute
        ↓
HighErrorRate = firing
        ↓
Alertmanager receives HighErrorRate
```

Esse cenário demonstra que o monitoramento consegue identificar degradação mesmo quando o `target` continua disponível.

---

## Evidências Persistidas

Diferentemente do primeiro cenário `ReliabilityApiDown`, durante o teste do `HighErrorRate` as evidências foram persistidas enquanto o alerta ainda estava ativo.

### Prometheus Alert Firing

Arquivo:

```text
high-error-rate-firing.txt
```

Esse arquivo registra o alerta `HighErrorRate` no Prometheus no estado:

```text
state    = firing
severity = warning
service  = reliability-api
```

Isso comprova que a condição configurada resultou em um alerta ativo no Prometheus durante a simulação.

### Alertmanager Active Alert

Arquivo:

```text
high-error-rate-alertmanager-active.txt
```

Esse arquivo registra o recebimento do alerta pelo Alertmanager.

Entre os dados registrados estão:

```text
alertname = HighErrorRate
service   = reliability-api
severity  = warning
state     = active
```

Isso demonstra o encaminhamento do alerta:

```text
Prometheus
     ↓
Alertmanager
```

### API Availability During Degradation

Arquivo:

```text
high-error-rate-api-up.txt
```

Esse arquivo registra que, durante o cenário de degradação, o Prometheus ainda observava:

```promql
up{job="reliability-api"} = 1
```

Isso é particularmente importante porque comprova que o cenário não representa indisponibilidade completa.

A situação observada foi:

```text
TARGET = UP
SERVICE = DEGRADED
```

---

## Evidência do Cenário

As evidências persistidas permitem reconstruir o seguinte estado operacional:

```text
Reliability API
      ↓
Target available
      ↓
up = 1
      ↓
HTTP 500 responses
      ↓
High Error Rate
      ↓
Prometheus
HighErrorRate = firing
      ↓
Alertmanager
state = active
```

Dessa forma, o repositório contém evidências verificáveis tanto da disponibilidade do `target` quanto da degradação detectada pela camada de alertas.

---

# Comparação dos Cenários

Os dois alertas representam problemas operacionais diferentes.

| Cenário | Target | Condição | Severidade | Resultado |
|---|---|---|---|---|
| `ReliabilityApiDown` | DOWN | `up == 0` | critical | indisponibilidade |
| `HighErrorRate` | UP | Error Rate > 10% + volume mínimo | warning | degradação |

O primeiro cenário responde à pergunta:

```text
O serviço está disponível para o Prometheus?
```

O segundo responde a uma pergunta diferente:

```text
Mesmo disponível, o serviço está respondendo adequadamente?
```

Essa diferença é essencial em monitoramento de sistemas distribuídos.

---

# Fluxo Geral de Monitoramento

Com os dois cenários implementados, a camada atual pode ser representada como:

```text
                    Reliability API
                           │
                           ↓
                       Prometheus
                           │
              ┌────────────┴────────────┐
              │                         │
              ↓                         ↓
           up == 0              HTTP 5xx Error Rate
              │                         │
              ↓                         ↓
   ReliabilityApiDown            HighErrorRate
              │                         │
              └────────────┬────────────┘
                           ↓
                      Alertmanager
```

Isso permite detectar tanto:

```text
HARD FAILURE
Target unavailable
```

quanto:

```text
SERVICE DEGRADATION
Target available but unhealthy responses
```

---

# Resultado Geral

Os testes realizados demonstram operacionalmente:

```text
MONITORING
    ↓
METRIC COLLECTION
    ↓
CONDITION EVALUATION
    ↓
ALERT DETECTION
    ↓
PROMETHEUS FIRING
    ↓
ALERTMANAGER
    ↓
EVIDENCE
```

O projeto passa a demonstrar capacidade de:

- monitorar disponibilidade por meio da métrica `up`;
- monitorar comportamento HTTP da aplicação;
- distinguir indisponibilidade de degradação;
- detectar respostas HTTP `5xx`;
- calcular Error Rate;
- considerar volume mínimo de tráfego;
- evitar alertas baseados em amostras pouco representativas;
- configurar regras no Prometheus;
- utilizar estados `inactive`, `pending` e `firing`;
- encaminhar alertas para o Alertmanager;
- validar recuperação do serviço;
- preservar evidências técnicas dos testes;
- evoluir uma regra de monitoramento a partir de comportamento observado durante a experimentação.

Esses cenários estabelecem uma base prática de **Monitoring**, **Alerting**, **Incident Detection**, **Service Degradation Detection** e **Service Recovery** aplicada ao projeto de SRE.
