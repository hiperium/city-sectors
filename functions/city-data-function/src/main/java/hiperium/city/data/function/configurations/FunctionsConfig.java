package hiperium.city.data.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.data.function.dto.CityDataRequest;
import hiperium.city.data.function.dto.CityDataResponse;
import hiperium.city.data.function.functions.CityDataFunction;
import hiperium.city.data.function.mappers.CityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods=false)
public class FunctionsConfig {

    public static final String FIND_BY_ID_BEAN_NAME = "findByIdFunction";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

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
    public Function<Message<CityDataRequest>, CityDataResponse> findByIdFunction() {
        LOGGER.info("Configuring CityData Function.");
        return new CityDataFunction(this.cityMapper, this.dynamoDbClient);
    }
}
