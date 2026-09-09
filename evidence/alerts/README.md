# Evidências de Alertas

## ReliabilityApiDown

O alerta `ReliabilityApiDown` foi validado por meio de um ciclo completo de falha, detecção e recuperação da aplicação.

O objetivo deste teste foi demonstrar, na prática, como a camada de monitoramento identifica a indisponibilidade da Reliability API, aciona um alerta e reconhece posteriormente a recuperação do serviço.

## Cenário de Falha

A Reliability API foi interrompida intencionalmente para simular uma indisponibilidade de serviço em um ambiente de produção.

Durante a interrupção, o Prometheus deixou de conseguir realizar o `scrape` das métricas da aplicação.

A métrica de disponibilidade do `target` passou a apresentar:

```promql
up{job="reliability-api"} = 0
```

A regra de alerta utilizada para detectar essa condição foi:

```promql
up{job="reliability-api"} == 0
```

A regra foi configurada com:

```text
for: 1m
```

Isso significa que a condição precisava permanecer verdadeira durante pelo menos 1 minuto antes de o alerta ser considerado ativo.

Após esse período, o alerta `ReliabilityApiDown` entrou no estado:

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

Com o `target` novamente disponível, a condição da regra:

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

As evidências abaixo foram armazenadas no repositório para permitir a inspeção dos resultados do teste.

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

As evidências atualmente armazenadas comprovam diretamente a etapa de recuperação:

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

Esse ciclo representa uma implementação inicial de práticas de **Monitoring**, **Alerting**, **Incident Detection** e **Service Recovery** aplicadas a um cenário de SRE.
