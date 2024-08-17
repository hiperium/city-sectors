package hiperium.city.read.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.CityDataResponse;
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
public class FunctionsConfig {

    public static final String FIND_BY_ID_BEAN_NAME = "findById";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

    private final CityMapper cityMapper;
    private final CitiesService citiesService;

    /**
     * This class represents the configuration for functions in the application.
     */
    public FunctionsConfig(CityMapper cityMapper, CitiesService citiesService) {
        this.cityMapper = cityMapper;
        this.citiesService = citiesService;
    }

    /**
     * Creates a bean that finds a city by its identifier.
     *
     * @return The function that finds a city by its identifier.
     */
    @Bean(FIND_BY_ID_BEAN_NAME)
    public Function<Message<byte[]>, Mono<CityDataResponse>> findByIdFunction() {
        LOGGER.info("Creating City Data Function bean...");
        return new ReadFunction(this.cityMapper, this.citiesService);
    }
}
