# Implementação dos Requisitos de Tolerância a Falhas

Este documento detalha como cada requisito da especificação foi implementado no serviço de E-commerce.

## Requisitos Gerais

### 1. Timeout Global de 1s
Implementado no `application.yml` através do Resilience4j TimeLimiter:
```yaml
resilience4j:
  timelimiter:
    instances:
      default:
        timeoutDuration: 1s
```

### 2. Parâmetro de Tolerância a Falhas
- Implementado no `PurchaseRequest` como o campo `ft` (boolean)
- Todas as chamadas de serviço em `PurchaseService` verificam este parâmetro antes de aplicar os mecanismos de tolerância

## Requisições

### Request 0 (Endpoint /buy)
Implementado em `PurchaseController`:
- Endpoint: POST `/buy`
- Parâmetros: 
  - `product` (Long) - ID do produto
  - `user` (Long) - ID do usuário
  - `ft` (boolean) - Flag de tolerância a falhas
- Retorno: ID da transação (gerado no Request 3)

### Request 1 (Consulta Produto)
Implementado em `PurchaseService.getProduct()`:
- Endpoint: GET `/product/{id}`
- Tolerância a Falha (Omission):
  - Circuit Breaker configurado
  - Fallback retorna um produto padrão em caso de falha

### Request 2 (Taxa de Câmbio)
Implementado em `PurchaseService.getExchangeRate()`:
- Endpoint: GET `/exchange`
- Tolerância a Falha (Crash):
  - Mantém último valor válido em `lastKnownRate`
  - Fallback usa o último valor conhecido

### Request 3 (Venda)
Implementado em `PurchaseService.processSale()`:
- Endpoint: GET `/sell/{id}`
- Tolerância a Falha (Error):
  - Circuit Breaker configurado
  - Fallback gera um ID de transação aleatório

### Request 4 (Bônus Fidelidade)
Implementado em `PurchaseService.registerBonus()`:
- Endpoint: POST `/bonus`
- Tolerância a Falha (Time):
  - Processamento assíncrono
  - Sistema de retry com lista de pendências
  - Modo de degradação por 30 segundos após falha
  - Background processor para tentar reprocessar bônus pendentes

## Mecanismos de Tolerância a Falhas

### 1. Exchange Service (Último valor válido)
- Implementado usando `AtomicReference<Double> lastKnownRate`
- Atualizado a cada chamada bem-sucedida
- Usado como fallback em caso de falha

### 2. Fidelity Service (Log e processamento posterior)
- Lista de requisições pendentes (`pendingFidelityRequests`)
- Modo de degradação temporário (`degradeModeStart`)
- Processador em background (`startPendingBonusProcessor`)
- Retry automático a cada 5 segundos

### 3. Store Service (Circuit Breaker)
- Circuit Breaker configurado para ambos endpoints (product e sell)
- Fallbacks implementados:
  - Produto: retorna produto padrão
  - Venda: gera ID de transação aleatório

## Configurações do Circuit Breaker
No `application.yml`:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      default:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```
