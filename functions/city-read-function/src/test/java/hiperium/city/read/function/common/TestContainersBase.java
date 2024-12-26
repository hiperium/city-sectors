package hiperium.city.read.function.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public abstract class TestContainersBase {

    private static final String LOCALSTACK_IMAGE = "localstack/localstack:latest";

    protected static final LocalStackContainer LOCALSTACK_CONTAINER;

    // Singleton containers.
    // See: https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
    static {
        LOCALSTACK_CONTAINER = new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE))
            .withServices(LocalStackContainer.Service.DYNAMODB)
            .withCopyToContainer(MountableFile.forClasspathResource("localstack/table-setup.sh"),
                "/etc/localstack/init/ready.d/table-setup.sh")
            .withCopyToContainer(MountableFile.forClasspathResource("localstack/table-data.json"),
                "/var/lib/localstack/table-data.json")
            .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()))
            .withEnv("DEBUG", "0")
            .withEnv("LS_LOG", "info")
            .withEnv("EAGER_SERVICE_LOADING", "1");

        LOCALSTACK_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.region.static", LOCALSTACK_CONTAINER::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", LOCALSTACK_CONTAINER::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", LOCALSTACK_CONTAINER::getSecretKey);
        registry.add("spring.cloud.aws.endpoint", () -> LOCALSTACK_CONTAINER.getEndpoint().toString());
    }
}
