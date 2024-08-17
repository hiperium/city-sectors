package hiperium.city.read.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;
import java.util.Objects;

/**
 * The DynamoDbClientConfig class provides a configuration for creating an instance of DynamoDbAsyncClient.
 */
@Configuration(proxyBeanMethods = false)
public class DynamoDbClientConfig {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(DynamoDbClientConfig.class);

    private final Environment environment;

    /**
     * Create an instance of DynamoDbClientConfig with the specified environment.
     *
     * @param environment the environment used for constructing the instance
     */
    public DynamoDbClientConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Returns an instance of DynamoDbAsyncClient.
     *
     * @return an instance of DynamoDbAsyncClient
     * @apiNote The async clients are not autoconfigured by the Spring Cloud AWS module.
     */
    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient() {
        var builder = DynamoDbAsyncClient.builder()
            .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
            .credentialsProvider(DefaultCredentialsProvider.builder().build());
        String endpointOverrideURL = this.environment.getProperty("spring.cloud.aws.endpoint");
        if (Objects.nonNull(endpointOverrideURL) && !endpointOverrideURL.isBlank()) {
            LOGGER.debug("DynamoDB Endpoint Override", endpointOverrideURL);
            builder.endpointOverride(URI.create(endpointOverrideURL));
        }
        return builder.build();
    }
}
