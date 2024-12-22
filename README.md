# Sistema E-commerce Tolerante a Falhas

## Visão Geral
Este projeto implementa um sistema de e-commerce distribuído com foco em tolerância a falhas. O sistema é completamente containerizado e utiliza arquitetura de microsserviços com comunicação REST.

## Características Principais
- Arquitetura baseada em microsserviços usando Spring Boot
- Comunicação REST entre serviços
- Timeout global de 1 segundo para todas as requisições
- Mecanismos de tolerância a falhas configuráveis via parâmetro
- Persistência com MongoDB
- Containerização com Docker

## Arquitetura

### Serviços

1. **Ecommerce Service** (`/buy`)
   - Ponto de entrada principal do sistema
   - Orquestra a comunicação entre os demais serviços
   - Implementa mecanismos de tolerância a falhas
   - Endpoint:
     - POST `/buy`: Processa compra com parâmetros:
       - product: ID do produto
       - user: ID do usuário
       - ft: flag de tolerância a falhas (true/false)

2. **Store Service** (`/product`, `/sell`)
   - Gerencia produtos e vendas
   - Endpoints:
     - GET `/product`: Retorna dados do produto (id, name, value)
     - POST `/sell`: Processa venda e retorna ID único da transação
   - Falhas programadas:
     - Request 1 (GET /product): Omission (p=0.2, d=0s)
     - Request 3 (POST /sell): Error (p=0.1, d=5s)

3. **Exchange Service** (`/exchange`)
   - Fornece taxa de conversão de moeda
   - Endpoint:
     - GET `/exchange`: Retorna taxa de conversão (número real positivo)
   - Falha programada:
     - Request 2: Crash (p=0.1, d=indefinido)

4. **Fidelity Service** (`/bonus`)
   - Gerencia programa de fidelidade
   - Endpoint:
     - POST `/bonus`: Registra pontos com parâmetros:
       - user: ID do usuário
       - bonus: valor inteiro do bônus
   - Falha programada:
     - Request 4: Time (p=0.1, d=30s, delay=2s)

## Mecanismos de Tolerância a Falhas

### Exchange Service
- Cache do último valor válido de taxa de conversão
- Usado quando o serviço falha (Crash)

### Fidelity Service
- Log e processamento assíncrono
- Permite continuar a compra mesmo com falha no serviço
- Processa bônus quando o serviço estiver disponível

### Store Service
- Circuit Breaker com Resilience4j
- Retry com backoff exponencial
- Cache local para dados de produtos
- Fallback para últimos valores conhecidos

## Requisitos Técnicos

### Pré-requisitos
- Docker e Docker Compose
- Java 17
- Maven

### Configuração
1. Clone o repositório
```bash
git clone [URL_DO_REPOSITORIO]
```

2. Build dos serviços
```bash
mvn clean package -DskipTests
```

3. Iniciar os containers
```bash
docker-compose up --build
```

### Testes
Para testar o sistema, envie uma requisição POST para `http://localhost:8080/buy`:

```json
{
    "product": "123",
    "user": "456",
    "ft": true
}
```

## Monitoramento
- Health checks via Spring Actuator
- Métricas do Resilience4j
- Logs centralizados
- Estado dos circuit breakers

## Documentação Adicional
- [ESPECIFICATION.md](ESPECIFICATION.md) - Especificação detalhada do projeto
- [TODO.md](TODO.md) - Lista de tarefas e progresso
