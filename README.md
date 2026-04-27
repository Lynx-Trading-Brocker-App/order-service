# order-service
Core trading microservice for the Broker Platform. Handles market, limit, and option orders while orchestrating synchronous updates to the Wallet and Portfolio services. Built with Java &amp; Spring Boot.

## Run modes

### Local app + Docker DB (recommended for development)
```powershell
docker-compose up -d
.\gradlew.bat bootRun
```

- DB runs on `localhost:5433`
- App runs locally on `localhost:8080`

### Full Docker stack (app + db)
```powershell
docker-compose --profile fullstack up -d
```

- App runs in container on `localhost:8080`
- DB runs on `localhost:5433`
