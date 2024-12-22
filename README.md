# Sistema E-commerce Tolerante a Falhas

## Visão Geral
Este projeto implementa um sistema de e-commerce distribuído com foco em tolerância a falhas. O sistema é completamente containerizado e utiliza arquitetura de microsserviços com comunicação REST.

## Características Principais
- Arquitetura baseada em microsserviços usando Spring Boot
- Comunicação REST entre serviços
- Timeout global de 1 segundo para todas as requisições
- Mecanismos de tolerância a falhas configuráveis via parâmetro
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

## Execução do Sistema

Para instruções detalhadas sobre como executar o sistema, consulte o arquivo [DEVELOPMENT.md](DEVELOPMENT.md).

## Testes

Para testar o sistema após a inicialização:

```bash
# Teste sem tolerância a falhas
curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=false"

# Teste com tolerância a falhas
curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=true"
```

## Documentação Adicional

- [DEVELOPMENT.md](DEVELOPMENT.md): Guia completo de desenvolvimento e configuração
- [REPORT.md](REPORT.md): Detalhes da implementação e análise do sistema

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request
