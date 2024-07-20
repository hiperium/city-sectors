## Hiperium City APIs (not implemented yet)

This project contains source code
and supports files for a containerized application
that you can deploy with the Copilot CLI. It includes the following files and folders.

- src/main - Code for the container application.
- src/test - Integration tests for the application code.
- src/main/resources - Spring Boot configuration files.


### Running using Docker Compose and LocalStack.

*IMPORTANT:* The GraalVM `native-image` compiler should be installed and configured on your machine. 
If you're experiencing JMV memory issues, execute the following commands to increase the JVM memory and execute Maven with more CPU cores:

```bash
export _JAVA_OPTIONS="-Xmx8g -Xms4g"
./mvnw -T 4C clean native:compile -Pnative -DskipTests -Ddependency-check.skip=true
```

Use the Hiperium Login command to authenticate with your AWS IAM Identity Center with the name of your IdP profile:
```bash
hiperium-login
```

Then, execute the main a shell script selecting option `1. Docker Compose.` to build and deploy the project locally using Docker Compose.
```bash
./start.sh
```

Open a new terminal tab and edit your `/etc/hosts` file adding a new entry point to access the API service using HTTPS:
```bash
vim /etc/hosts
```

Add the following line and save the file:
```bash
127.0.0.1 dev.hiperium.cloud
```

Open your Postman and import the collection `Hiperium.postman_collection.json` to test the API endpoints.

## Running Integration Tests against Native Image.
You can also run your existing tests suite in a native image.
This is an efficient way to validate the compatibility of your application:
```bash
./mvnw -T 2C test -PnativeTest
```

## Generate Lightweight Container with the Cloud Native Buildpacks
If you're already familiar with Spring Boot container images support, this is the easiest way to get started.
Docker should be installed and configured on your machine prior to creating the image.

To create the image, run the following goal:
```bash
./mvnw spring-boot:build-image -Pnative -DskipTests
```

## Generate Native Executable with the Native Build Tools
Use this option if you want to explore more options such as running your tests in a native image.
The GraalVM `native-image` compiler should be installed and configured on your machine.

**NOTE:** GraalVM 22.3+ is required.

To create the executable, run the following goal:
```bash
./mvnw native:compile -Pnative -DskipTests
```

## Getting Device items from DynamoDB on LocalStack.
Execute the following command:
```bash
awslocal dynamodb scan --table-name <TableName>
```

## AWS Copilot CLI Helpful Commands.

* List all of your AWS Copilot applications.
```bash
copilot app ls
```

* Show information about the environments and services in your application.
```bash
copilot app show
```

* Show information about your environments.
```bash
copilot env ls
```

* List of all the services in an application.
```bash
copilot svc ls
```

* Show service status.
```bash
copilot svc status
```

* Show information about the service, including endpoints, capacity and related resources.
```bash
copilot svc show
```

* Show logs of a deployed service.
```bash
export AWS_PROFILE=city-dev
copilot svc logs            \
    --app city-<app-name>   \
    --name api              \
    --env dev               \
    --startDate 1h          \
    --follow
```

* Start an interactive bash session with a taskEntity part of the service:
```bash
copilot svc exec             \
    --app city-<app-name>    \
    --name api               \
    --env dev
```

* Delete and clean-up all created resources.
```bash
copilot app delete --yes
```
