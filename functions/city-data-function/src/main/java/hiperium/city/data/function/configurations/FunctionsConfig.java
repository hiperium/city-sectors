package hiperium.city.data.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.data.function.dto.CityDataResponse;
import hiperium.city.data.function.functions.CityDataFunction;
import hiperium.city.data.function.mappers.CityMapper;
import hiperium.city.data.function.repository.CitiesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods = false)
public class FunctionsConfig {

    public static final String FIND_BY_ID_BEAN_NAME = "findByIdFunction";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

    private final CityMapper cityMapper;
    private final CitiesRepository citiesRepository;

    /**
     * This class represents the configuration for functions in the application.
     *
     * @param cityMapper        The CityMapper object used for mapping attribute values of a City object.
     * @param citiesRepository  The CitiesRepository object used for retrieving City objects from the DynamoDB table.
     */
    public FunctionsConfig(CityMapper cityMapper, CitiesRepository citiesRepository) {
        this.cityMapper = cityMapper;
        this.citiesRepository = citiesRepository;
    }

    /**
     * Creates a bean that finds a city by its identifier.
     *
     * @return The function that finds a city by its identifier.
     */
    @Bean(FIND_BY_ID_BEAN_NAME)
    public Function<Message<byte[]>, Mono<CityDataResponse>> findByIdFunction() {
        LOGGER.info("Creating City Data Function bean...");
        return new CityDataFunction(this.cityMapper, this.citiesRepository);
    }
}
