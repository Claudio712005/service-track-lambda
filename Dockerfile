FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean build -x test \
    -Dquarkus.package.jar.type=legacy-jar


FROM public.ecr.aws/lambda/java:21 AS runtime


COPY --from=build /workspace/build/*-runner.jar  ${LAMBDA_TASK_ROOT}/lib/
COPY --from=build /workspace/build/lib/          ${LAMBDA_TASK_ROOT}/lib/

CMD ["io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"]
