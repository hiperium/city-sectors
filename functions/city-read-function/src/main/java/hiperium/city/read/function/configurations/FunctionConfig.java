package hiperium.city.read.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.ReadCityResponse;
import hiperium.city.read.function.functions.ReadFunction;
import hiperium.city.read.function.mappers.CityMapper;
import hiperium.city.read.function.services.CitiesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * This class represents the configuration for functions in the application.
 */
@Configuration(proxyBeanMethods = false)
public class FunctionConfig {

    public static final String FUNCTION_BEAN_NAME = "findById";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionConfig.class);

    private final CityMapper cityMapper;
    private final CitiesService citiesService;

    /**
     * This class represents the configuration for functions in the application.
     */
    public FunctionConfig(CityMapper cityMapper, CitiesService citiesService) {
        this.cityMapper = cityMapper;
        this.citiesService = citiesService;
    }

    /**
     * Creates a bean that finds a city by its identifier.
     *
     * @return The function that finds a city by its identifier.
     */
    @Bean(FUNCTION_BEAN_NAME)
    public Function<Message<byte[]>, Mono<ReadCityResponse>> findByIdFunction() {
        LOGGER.info("Creating City Data Function bean...");
        return new ReadFunction(this.cityMapper, this.citiesService);
    }
}
