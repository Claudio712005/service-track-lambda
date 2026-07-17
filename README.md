# service-track-auth-lambda

Serviço de autenticação do ServiceTrack. Valida credenciais de usuário e emite tokens JWT.
Construído com Quarkus + Kotlin e empacotado para execução como AWS Lambda.

## Stack

- Kotlin 2.4 / JVM 21
- Quarkus 3.37.2
- Hibernate ORM com Panache (Kotlin)
- PostgreSQL (JDBC)
- SmallRye JWT (assinatura/verificação RS256 via chaves PEM)
- AWS Lambda (quarkus-amazon-lambda-rest)
- Gradle (wrapper)

## Arquitetura

Arquitetura hexagonal (ports & adapters):

```
src/main/kotlin/br/com/servicetrack/
  application/          Casos de uso e DTOs de entrada/saída
    dto/
    service/            AutenticacaoService
  domain/               Núcleo de negócio, sem dependências de framework
    model/              Usuario, TokenAutenticacao
    vo/                 Value objects: Cpf, Email, Senha, UsuarioId
    ports/in/           Contratos de entrada (IAutenticacaoUseCase)
    ports/out/          Contratos de saída (IUsuarioRepositoryPort, ITokenProviderPort)
    exception/          Exceções de domínio
    shared/enums/       Role (CLIENTE, MECANICO)
  infrastructure/       Adaptadores concretos
    adapter/resource/   Endpoints REST (AutenticacaoResource)
    adapter/security/   JwtTokenProvider, BcryptVerificadorSenha
    adapter/web/         ExceptionMappers, RequestIdFilter
    persistence/        UsuarioEntity, UsuarioRepository
```

O `domain` não conhece Quarkus. As dependências apontam para dentro, das camadas externas
para os ports do domínio.

## Endpoints

### POST /autenticacao/login

Autentica um usuário e retorna um token JWT.

Requisição:

```json
{
  "email": "usuario@servicetrack.com.br",
  "senha": "senhaForte123"
}
```

Resposta (200):

```json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "expiraEmSegundos": 3600
}
```

Validações: `email` deve ser válido e não vazio; `senha` mínimo de 8 caracteres.
Credenciais inválidas ou usuário inativo retornam erro de credenciais.

## Chaves JWT (PEM)

O serviço assina tokens com uma chave privada RSA e os verifica com a chave pública.

| Arquivo | Uso | Versionado |
|---------|-----|------------|
| `src/main/resources/publicKey.pem` | Verificação (`mp.jwt.verify.publickey.location`) | Sim (chave pública) |
| `src/main/resources/privateKey.pem` | Assinatura (`smallrye.jwt.sign.key.location`) | Não (gitignored) |

A chave privada nunca deve ser commitada. Ela está no `.gitignore` e a pipeline de CI
falha se qualquer chave privada for versionada. Em produção, forneça a chave privada
via segredo/variável de ambiente no runtime da Lambda.

Gerar um par RS256 para desenvolvimento local:

```shell
openssl genpkey -algorithm RSA -out src/main/resources/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/privateKey.pem -out src/main/resources/publicKey.pem
```

## Configuração

Definida em `src/main/resources/application.properties`.

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `POSTGRES_HOST` | `localhost` | Host do PostgreSQL |
| `POSTGRES_PORT` | `5432` | Porta do PostgreSQL |
| `POSTGRES_DB` | `servicetrack` | Nome do banco |
| `POSTGRES_USER` | `postgres` | Usuário do banco |
| `POSTGRES_PASSWORD` | `postgres` | Senha do banco |
| `servicetrack.jwt.expiracao-segundos` | `3600` | Validade do token em segundos |
| `mp.jwt.verify.issuer` | `https://servicetrack.com.br/auth` | Issuer esperado no JWT |

O perfil `local` (`application-local.properties`) sobrescreve o nível de log e a URL do banco.

## Banco de dados

Schema em `src/main/resources/db/schema.sql` (tabelas `usuarios` e `usuario_roles`).
`quarkus.hibernate-orm.schema-management.strategy=none` — o schema não é gerado pela aplicação;
aplique o SQL manualmente ou via migração.

## Execução

Modo de desenvolvimento (live reload, Dev UI em <http://localhost:8080/q/dev/>):

```shell
./gradlew quarkusDev
```

Build e testes:

```shell
./gradlew build
```

Executar o jar empacotado:

```shell
java -jar build/quarkus-app/quarkus-run.jar
```

Executável nativo:

```shell
./gradlew build -Dquarkus.native.enabled=true
# Sem GraalVM local, buildar em container:
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

## Testes

```shell
./gradlew test
```

Testes de unidade usam fakes dos ports, sem necessidade de banco ou chaves PEM.

## Docker / Lambda

O `Dockerfile` na raiz faz build multi-stage (JDK 21) e empacota como imagem de Lambda
(`public.ecr.aws/lambda/java:21`), usando o handler
`io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler`.

```shell
docker build -t service-track-auth-lambda .
```

## CI

Pipeline em `.github/workflows/ci.yml` roda em push e pull request para `main`:

- `secret-guard`: falha se alguma chave privada estiver versionada.
- `build`: valida o Gradle wrapper, roda testes e build em JDK 21, publica relatórios de teste.
