# Scaffolding project for a Ktor server application

## Features

* Dependency Injection (Koin)
* Database connection pooling (Hikari)
* (basic) Transaction management
    * TODO: More control  
* Database migrations (Flyway)
* Query Building & Execution (JOOQ, codegen from database migrations) 
* DTO validation (Hibernate validator)
* API Documentation (Swagger, protected by BasicAuth and a configurable password)
* Global error handling
* Authentication (via token in custom header `X-Auth-Token`)
* Authorization
* Parallel test execution
* `dev` package that's excluded from the built jar to avoid dangerous code on live systems. 
    It contains for example the fixture loader that drops the schema before inserting it's data.

## How to run: 

1. Create a run configuration for `my.company.app.KtorMainKt`
2. Add environment variable `PROFILE=local-dev`
3. Start the containers: `cd docker; docker-compose up -d`
4. Start the server

## Swagger

http://localhost:8080/swagger/index.html?url=swagger.json

In local dev mode, BasicAuth username and password will be `swagger`

## Intellij Code Style

Import `intellij-codestyle.xml` via `Preferences` -> `Settings` -> `Code Style`

## Detekt

A pre-commit hook is automatically installed when running gradle.
It will only run detekt on VCS changed files
To manually run detekt execute `./scripts/detekt.sh`

## Tests

Tests are setup to run concurrently by default.
Any test that does not support concurrency (i.e: postgres integration tests) need to be annotated with `@Execution(ExecutionMode.SAME_THREAD)`

## TODOs

* Parse query parameters as data classes
