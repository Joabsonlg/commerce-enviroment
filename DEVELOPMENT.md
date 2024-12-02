# Guia de Desenvolvimento - Sistema E-commerce Tolerante a Falhas

## Visão Geral da Arquitetura

O sistema é composto por quatro microserviços principais:

1. **Ecommerce Service** (Orquestrador)
   - Gerencia o fluxo de compras
   - Integra com outros serviços
   - Implementa circuit breakers

2. **Store Service**
   - Gerencia produtos e estoque
   - Processa vendas
   - Mantém consistência do inventário

3. **Exchange Service**
   - Fornece taxas de câmbio
   - Implementa cache com Redis
   - Possui fallback para indisponibilidade

4. **Fidelity Service**
   - Gerencia pontos de fidelidade
   - Processa bônus assincronamente
   - Implementa retry pattern

## Ambiente de Desenvolvimento

### Requisitos

- Java 17
- Maven 3.8+
- Docker Desktop
- Git

### Instalação do Docker Desktop (Windows)

1. **Requisitos do Sistema**
   - Windows 10/11 64-bit: Pro, Enterprise, ou Education
   - WSL 2
   - Virtualização habilitada

2. **Instalar WSL 2**
   ```powershell
   # Abra o PowerShell como Administrador e execute:
   wsl --install
   ```

3. **Habilitar Virtualização**
   - Reinicie o computador
   - Entre na BIOS (geralmente tecla Delete ou F2 durante o boot)
   - Procure e habilite opções como:
     - "Virtualization Technology"
     - "VT-x" (Intel)
     - "AMD-V" ou "SVM" (AMD)
   - Salve e reinicie

4. **Instalar Docker Desktop**
   - Baixe em: https://www.docker.com/products/docker-desktop/
   - Execute o instalador
   - Siga o assistente de instalação
   - Reinicie o computador

5. **Verificar Instalação**
   ```bash
   # Verificar versão do Docker
   docker --version

   # Testar instalação
   docker run hello-world
   ```

### Setup do Ambiente

1. **Clone do Repositório**
   ```bash
   git clone <repository-url>
   cd tf-workspace
   ```

2. **Instalação do Redis**
   ```bash
   # Inicie o Redis via Docker
   docker run --name redis -p 6379:6379 -d redis:latest
   
   # Verifique se o Redis está rodando
   docker ps
   ```

3. **Build do Projeto**
   ```bash
   # Build completo com testes
   mvn clean install

   # Build rápido sem testes
   mvn clean install -DskipTests
   ```

### Executando os Serviços Localmente

1. **Store Service (Porta 8081)**
   ```bash
   # Via Maven
   cd services/store
   mvn spring-boot:run

   # Via Java
   cd services/store/target
   java -jar store-1.0.0.jar
   ```
   - Swagger UI: http://localhost:8081/swagger-ui.html
   - Health Check: http://localhost:8081/actuator/health
   - Métricas: http://localhost:8081/actuator/prometheus

2. **Exchange Service (Porta 8082)**
   ```bash
   # Via Maven
   cd services/exchange
   mvn spring-boot:run

   # Via Java
   cd services/exchange/target
   java -jar exchange-1.0.0.jar
   ```
   - Swagger UI: http://localhost:8082/swagger-ui.html
   - Health Check: http://localhost:8082/actuator/health
   - Métricas: http://localhost:8082/actuator/prometheus

3. **Fidelity Service (Porta 8083)**
   ```bash
   # Via Maven
   cd services/fidelity
   mvn spring-boot:run

   # Via Java
   cd services/fidelity/target
   java -jar fidelity-1.0.0.jar
   ```
   - Swagger UI: http://localhost:8083/swagger-ui.html
   - Health Check: http://localhost:8083/actuator/health
   - Métricas: http://localhost:8083/actuator/prometheus

4. **Ecommerce Service (Porta 8080)**
   ```bash
   # Via Maven
   cd services/ecommerce
   mvn spring-boot:run

   # Via Java
   cd services/ecommerce/target
   java -jar ecommerce-1.0.0.jar
   ```
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health
   - Métricas: http://localhost:8080/actuator/prometheus

### Ordem de Inicialização Recomendada
1. Redis (via Docker Desktop)
2. Store Service
3. Exchange Service
4. Fidelity Service
5. Ecommerce Service

### Verificação do Ambiente

1. **Verificar Status dos Serviços**
   ```bash
   # Verificar Store Service
   curl http://localhost:8081/actuator/health

   # Verificar Exchange Service
   curl http://localhost:8082/actuator/health

   # Verificar Fidelity Service
   curl http://localhost:8083/actuator/health

   # Verificar Ecommerce Service
   curl http://localhost:8080/actuator/health
   ```

2. **Verificar Redis**
   ```bash
   # Via Docker
   docker exec -it redis redis-cli ping
   # Deve retornar: PONG
   ```

### Análise de Código

1. **Executar Checkstyle**
   ```bash
   mvn checkstyle:check
   ```
   - Relatório: `target/site/checkstyle.html`

2. **Executar SpotBugs**
   ```bash
   mvn spotbugs:check
   ```
   - Relatório: `target/spotbugsXml.xml`

3. **Executar Todos os Testes**
   ```bash
   mvn test
   ```

### Troubleshooting

1. **Docker/Redis**
   ```bash
   # Verificar status do Docker
   docker ps
   docker info

   # Se o Docker não estiver rodando:
   # 1. Abra o Docker Desktop
   # 2. Aguarde a inicialização completa
   # 3. Verifique se o ícone do Docker na bandeja está verde
   ```

2. **Portas em Uso**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   netstat -ano | findstr :8081
   netstat -ano | findstr :8082
   netstat -ano | findstr :8083
   ```

3. **Logs dos Serviços**
   - Logs são escritos em `logs/` de cada serviço
   - Nível de log pode ser ajustado em `application.properties`

4. **Redis não Conecta**
   ```bash
   # Verificar status do container
   docker ps -a | grep redis
   
   # Reiniciar Redis se necessário
   docker restart redis

   # Se precisar remover e recriar
   docker rm -f redis
   docker run --name redis -p 6379:6379 -d redis:latest
   ```

5. **Limpeza do Ambiente**
   ```bash
   # Parar todos os serviços (Windows)
   taskkill /F /IM java.exe
   
   # Parar Redis
   docker stop redis
   
   # Limpar builds
   mvn clean
   ```

## Referências

- [Docker Desktop Installation Guide](https://docs.docker.com/desktop/install/windows-install/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Circuit Breaker](https://spring.io/projects/spring-cloud-circuitbreaker)
- [Redis Documentation](https://redis.io/documentation)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)
- [Swagger/OpenAPI Documentation](https://swagger.io/docs/)
