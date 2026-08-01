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

### POST /autenticacao

Autentica um usuário por **CPF** e retorna um token JWT.

Requisição:

```json
{
  "cpf": "12345678901",
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

Validações: `cpf` com 11 dígitos, com ou sem pontuação (`123.456.789-01` também é aceito);
`senha` com mínimo de 8 caracteres. Credenciais inválidas ou usuário inativo retornam erro de
credenciais.

> O path é `/autenticacao`, sem sufixo. É o mesmo que o contrato do API Gateway expõe em
> `apis/service-track-api-ext/openApi.yaml` no repositório `service-track-aws-iac`. Os dois
> precisam mudar juntos: divergência aqui quebra em runtime e nenhum teste de contrato atual
> detecta.

## Chaves JWT (PEM)

O serviço assina tokens com uma chave privada RSA e os verifica com a chave pública.

| Arquivo | Uso | Versionado |
|---------|-----|------------|
| `src/main/resources/publicKey.pem` | Verificação (`mp.jwt.verify.publickey.location`) | Sim (chave pública) |
| `src/main/resources/privateKey.pem` | Assinatura (`smallrye.jwt.sign.key.location`) | Não (gitignored) |

A chave privada nunca deve ser commitada. Ela está no `.gitignore` e a pipeline de CI
falha se qualquer chave privada for versionada.

**Em `hml` e `prd` o par não é gerado à mão.** O Terraform do
[service-track-aws-iac](https://github.com/Claudio712005/service-track-aws-iac) cria um par por
ambiente e o entrega a esta função e à aplicação no mesmo apply, por variável de ambiente
(`MP_JWT_VERIFY_PUBLICKEY`, `SMALLRYE_JWT_SIGN_KEY`). Ver `IAC-ADR-018`.

Gerar um par RS256 para desenvolvimento local:

```shell
openssl genpkey -algorithm RSA -out src/main/resources/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/privateKey.pem -out src/main/resources/publicKey.pem
```

## CI/CD

| Esteira | Dispara em | O que faz |
|---|---|---|
| **CI** | push e PR | `secret-guard` falha se houver chave privada versionada; `build` valida o wrapper do Gradle, roda os testes em JDK 21 e publica os relatórios |
| **CD** | push na `main`, ou Run workflow | publica a imagem e atualiza a função |

O CD constrói a imagem, publica em `servicetrack-<env>-auth-lambda`, aplica o portão de
vulnerabilidade e chama `aws lambda update-function-code` em `servicetrack-<env>-auth`.

Push na `main` entrega em `hml`. Promoção para `prd` é manual, e informando `image_tag`
promove uma imagem já publicada sem reconstruir.

Os nomes do repositório e da função são **derivados do ambiente**, não recebidos de fora: o
padrão é `servicetrack-<env>-*`. Por isso nenhum repositório precisa escrever variável neste.

A esteira confere que a função existe antes de construir, e para com mensagem explícita se a
infraestrutura do ambiente ainda não foi aplicada.

> O Terraform gerencia a infraestrutura da função, não o código
> (`lifecycle { ignore_changes = [image_uri] }`). Por isso o deploy não exige `terraform apply`
> — importante numa conta cujo login expira a cada laboratório.

### Secrets necessárias

| Secret | Onde |
|---|---|
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` | Environments `hml` e `prd` deste repositório |

Renovadas a cada laboratório da AWS Academy.

---

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


