package hiperium.city.read.function.configurations;

import hiperium.cities.common.loggers.HiperiumLogger;
import hiperium.cities.common.responses.FunctionResponse;
import hiperium.city.read.function.functions.FindCityFunction;
import hiperium.city.read.function.functions.FindSectorsFunction;
import hiperium.city.read.function.requests.FunctionRequest;
import hiperium.city.read.function.services.CityService;
import org.springframework.cloud.function.context.DefaultMessageRoutingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Configuration class for setting up functional beans related to city and sector data processing.
 * This class declares beans for functions that handle requests to find city and sector details based on city IDs.
 * It also provides a default message routing handler to process unroutable messages.
 */
@Configuration(proxyBeanMethods = false)
public class FunctionConfig {

    public static final String CITY_FUNCTION_BEAN_NAME = "findCityById";
    public static final String SECTOR_FUNCTION_BEAN_NAME = "findSectorsByCityId";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionConfig.class);

    private final CityService cityService;

    public FunctionConfig(CityService cityService) {
        this.cityService = cityService;
    }

    @Bean(CITY_FUNCTION_BEAN_NAME)
    public Function<Message<FunctionRequest>, Mono<FunctionResponse>> findCityByIdFunction() {
        LOGGER.debug("Creating City Data function.");
        return new FindCityFunction(this.cityService);
    }

    @Bean(SECTOR_FUNCTION_BEAN_NAME)
    public Function<Message<FunctionRequest>, Mono<FunctionResponse>> findSectorsByCityIdFunction() {
        LOGGER.debug("Creating Sector Data function.");
        return new FindSectorsFunction(this.cityService);
    }

    @Bean
    public DefaultMessageRoutingHandler defaultRoutingHandler() {
        LOGGER.debug("Creating non-routable message handler.");
        return new NonRoutableMessageHandler();
    }
}
