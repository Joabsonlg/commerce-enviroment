# Sistema E-commerce Tolerante a Falhas

## Visão Geral
Este projeto implementa um sistema de e-commerce distribuído com foco em tolerância a falhas. O sistema é completamente containerizado e utiliza arquitetura de microsserviços com comunicação REST.

## Características Principais
- Arquitetura baseada em containers
- Comunicação REST entre serviços
- Timeout global de 1 segundo para todas as requisições
- Sistema de cache para valores de câmbio em caso de falhas
- Replicação de serviços críticos
- Falhas programadas para teste de resiliência

## Arquitetura

### Serviços

1. **Ecommerce Service** (`/buy`)
   - Ponto de entrada principal do sistema
   - Orquestra a comunicação entre os demais serviços
   - Implementa circuit breaker para chamadas aos serviços dependentes

2. **Store Service** (`/product`, `/sell`)
   - Gerencia informações dos produtos
   - Processa vendas
   - Endpoints:
     - `/product`: Consulta informações do produto
     - `/sell`: Processa a venda

3. **Exchange Service** (`/exchange`)
   - Serviço de câmbio com múltiplas réplicas
   - Implementa cache local para último valor em caso de falhas
   - Tolerante a falhas através de redundância

4. **Fidelity Service** (`/bonus`)
   - Gerencia programa de fidelidade
   - Calcula e atribui bônus nas compras

## Fluxo de Compra
1. Cliente acessa `/buy`
2. Sistema consulta `/product`
3. Realiza conversão monetária via `/exchange`
4. Processa venda através de `/sell`
5. Atribui pontos de fidelidade via `/bonus` (opcional, caso falhe, a compra continua, e o bônus fica pendente pra ser realizado quando o serviço de fidelidade estiver disponível)
6. Resposta ao cliente

## Mecanismos de Tolerância a Falhas

### Circuit Breaker
- Implementado em chamadas entre serviços
- Previne sobrecarga do sistema em caso de falhas
- Estado meio-aberto para tentativas graduais de reconexão

### Cache
- Cache local para taxas de câmbio
- TTL configurável
- Fallback para último valor conhecido em caso de falha

### Timeout
- Timeout global de 1 segundo
- Previne bloqueio de recursos por tempo indefinido
- Garante resposta rápida mesmo em cenários de falha

### Replicação
- Múltiplas instâncias do serviço de câmbio
- Load balancing entre réplicas
- Failover automático em caso de falha

### Retry Pattern
- Tentativas automáticas em caso de falhas temporárias
- Backoff exponencial para evitar sobrecarga
- Número máximo de tentativas configurável

## Cenários de Falha Programados
- Latência alta no serviço de câmbio
- Indisponibilidade temporária do serviço de fidelidade
- Erro intermitente no processamento de vendas

## Monitoramento
- Métricas de disponibilidade por serviço
- Tempo de resposta
- Taxa de erro
- Estado dos circuit breakers
- Hit rate do cache

## Documentação
- [TODO.md](TODO.md) - Lista de tarefas e progresso do projeto
- [DEPLOYMENT.md](DEPLOYMENT.md) - Guia completo de implantação do sistema
- [DEVELOPMENT.md](DEVELOPMENT.md) - Guia de desenvolvimento e boas práticas

## Próximos Passos
- Implementação dos serviços
- Configuração dos containers
- Implementação dos mecanismos de tolerância a falhas
- Testes de resiliência
- Documentação das APIs
