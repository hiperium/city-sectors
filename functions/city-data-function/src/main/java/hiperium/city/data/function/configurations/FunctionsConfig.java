package hiperium.city.data.function.configurations;

import hiperium.city.data.function.dto.CityIdRequest;
import hiperium.city.data.function.dto.CityResponse;
import hiperium.city.data.function.functions.CityDataFunction;
import hiperium.city.data.function.mappers.CityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods=false)
public class FunctionsConfig {

    public static final String FIND_BY_ID_BEAN_NAME = "findById";

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionsConfig.class);

    private final CityMapper cityMapper;
    private final DynamoDbClient dynamoDbClient;

    public FunctionsConfig(CityMapper cityMapper, DynamoDbClient dynamoDbClient) {
        this.cityMapper = cityMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Creates a bean that finds a city by its identifier.
     *
     * @return The function that finds a city by its identifier.
     */
    @Bean(FIND_BY_ID_BEAN_NAME)
    public Function<Mono<CityIdRequest>, Mono<CityResponse>> findById() {
        LOGGER.debug("Configuring CityData Function...");
        return new CityDataFunction(this.cityMapper, this.dynamoDbClient);
    }
}
