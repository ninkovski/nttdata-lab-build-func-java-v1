# Laboratorio Azure Functions - NTT Data

Este laboratorio proporciona una función de ejemplo en Java 17 y un pipeline completo de CI/CD en GitHub Actions que:
- Valida el código con tests unitarios (`mvn test`).
- Gestiona infraestructura en Azure de manera idempotente (Resource Group, Storage Account, App Service Plan, Function App).
- Despliega con el plugin `azure-functions-maven-plugin`.
- Ejecuta smoke tests contra la URL real desplegada.
- Sugiere auto-eliminación para control de costos.

## Requisitos previos
- Java 17
- Maven 3.9+
- Cuenta de Azure y permisos para crear recursos
- Secrets en GitHub: `AZURE_CREDENTIALS` (Service Principal con permisos sobre la suscripción)
- Branch naming: `release/<codigo-empleado>` (ej: `release/emp123`)

## Estructura
- `src/main/java/com/nttdata/lab/LabFunction.java`: Endpoints `saludar`, `calcular`, `health`.
- `src/test/java/com/nttdata/lab/LabFunctionTest.java`: Tests que deben pasar para desplegar.
- `.github/workflows/azure-functions-ci-cd.yml`: Pipeline CI/CD.
- `pom.xml`: Configuración Maven y plugin de Azure Functions.

## Variables de despliegue
Se generan a partir del nombre de la rama:
- Resource Group: `rg-lab-ntt-<id>`
- Function App: `fn-lab-ntt-<id>`
- Storage Account: `stlabntt<id>`
- App Service Plan: `asp-lab-ntt-<id>`

## Flujo del Pipeline
1. Extrae el ID del empleado desde `release/<id>`.
2. Ejecuta `mvn test` (falla el pipeline si algún test falla).
3. Empaqueta la app `mvn clean package -DskipTests`.
4. Login con `azure/login` usando `AZURE_CREDENTIALS` del environment `dev`.
5. Crea/omita Resource Group, Storage, Plan y Function App (idempotente).
6. Despliega con `mvn azure-functions:deploy`.
7. Espera 30s y ejecuta smoke tests (`health`, `saludar`, `calcular`).
8. Publica URLs en el resumen del job.
9. Sugiere eliminación del RG (`az group delete --no-wait`).

## Cómo ejecutar localmente
1. Instala Azure Functions Core Tools y emulador de storage (o usa `UseDevelopmentStorage=true`).
2. Ejecuta: `mvn clean package`.
3. Inicia funciones local: `mvn azure-functions:run`.
4. Prueba endpoints:
   - `GET http://localhost:7071/api/health`
   - `GET http://localhost:7071/api/saludar?nombre=TuNombre`
   - `GET http://localhost:7071/api/calcular?a=5&b=3`

## Notas de seguridad
- No expongas secretos en logs.
- Usa el environment `dev` para heredar `AZURE_CREDENTIALS`.
- Los nombres de recursos se derivan de la rama para aislamiento por alumno.
