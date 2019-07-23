# Scaffolding project for a Ktor server application

## Features

* Dependency Injection (Koin)
* Database connection pooling (Hikari)
    * TODO: Configurable data source
* (basic) Transaction management
    * TODO: More control  
* Database migrations (Flyway)
* Query Building & Execution (JOOQ, codegen from database migrations) 
* DTO validation (Hibernate validator)
* API Documentation (Swagger, protected by BasicAuth and a configurable password)
* Global error handling
* Authentication (via token in custom header `X-Auth-Token`)
* Authorization

## How to run: 

1. Create a run configuration for `my.company.app.KtorMainKt`
2. Add environment variable `PROFILE=local-dev`

## Swagger

http://localhost:8080/swagger/index.html?url=swagger.json

## TODOs

* Fixtures (To be loaded when the `load-fixtures` profile is active)
* Database hooks? (automated `created_at`, `updated_at`, etc?)
* Testing (Integration)
