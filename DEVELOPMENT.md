# Guia de Desenvolvimento - Sistema E-commerce Tolerante a Falhas

## Requisitos do Sistema

### Software Necessário
- Docker Desktop
- Git

### Requisitos do Docker Desktop (Windows)
- Windows 10/11 64-bit: Pro, Enterprise, ou Education
- WSL 2 habilitado
- Virtualização habilitada na BIOS

## Configuração do Ambiente

### 1. Instalação do Docker Desktop
1. Baixe o Docker Desktop do [site oficial](https://www.docker.com/products/docker-desktop)
2. Execute o instalador
3. Siga as instruções de instalação
4. Reinicie o computador se solicitado

### 2. Verificação da Instalação
```bash
docker --version
docker-compose --version
```

## Executando o Sistema

### 1. Clone o Repositório
```bash
git clone https://github.com/Joabsonlg/commerce-enviroment.git
cd commerce-enviroment
```

### 2. Realizar Packaging dos Serviços
```bash
# Na raiz do projeto
cd services
mvn clean package
cd ..
```

### 3. Iniciando os Serviços
```bash
# Na raiz do projeto
docker-compose up --build
```

Isso irá:
1. Construir as imagens dos serviços
2. Criar a rede Docker
3. Iniciar todos os containers
4. Configurar as dependências entre serviços

### 4. Verificando os Serviços
Após a inicialização, verifique se todos os serviços estão rodando:
```bash
docker-compose ps
```

Os serviços estarão disponíveis nas seguintes portas:
- E-commerce Service: http://localhost:8080
- Store Service: http://localhost:8081
- Exchange Service: http://localhost:8082
- Fidelity Service: http://localhost:8083

## Testando o Sistema

### 1. Teste Básico (Sem Falhas)
```bash
curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=false"
```

### 2. Teste com Tolerância a Falhas
```bash
curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=true"
```

### 3. Teste de Falhas Específicas

#### Store Service - Omission (20%)
```bash
# Fazer várias requisições para ver a falha
for i in {1..10}; do
    curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=true"
    sleep 1
done
```

#### Exchange Service - Crash (10%)
```bash
# O serviço pode entrar em crash
curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=true"
```

#### Fidelity Service - Time (10%, 30s)
```bash
# Observe o processamento assíncrono
curl -X POST "http://localhost:8080/buy?product=1&user=1&ft=true"
```

## Monitoramento

### Logs dos Serviços
```bash
# Ver logs de um serviço específico
docker-compose logs -f ecommerce

# Ver logs de todos os serviços
docker-compose logs -f
```

### Status dos Containers
```bash
docker-compose ps
```

## Troubleshooting

### Problemas Comuns

1. **Portas em Uso**
```bash
# Verificar portas em uso
netstat -ano | findstr :8080
netstat -ano | findstr :8081
netstat -ano | findstr :8082
netstat -ano | findstr :8083
```

2. **Containers não Iniciam**
```bash
# Parar todos os containers
docker-compose down

# Remover volumes
docker-compose down -v

# Reconstruir e iniciar
docker-compose up --build
```

3. **Problemas de Rede**
```bash
# Verificar rede Docker
docker network ls
docker network inspect tf-workspace_default
```

## Parando o Sistema
```bash
# Para parar mantendo os volumes
docker-compose down

# Para parar e remover volumes
docker-compose down -v
```
