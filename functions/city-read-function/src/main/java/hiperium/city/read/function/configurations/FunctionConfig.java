package hiperium.city.read.function.configurations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.responses.FunctionResponse;
import hiperium.city.read.function.functions.ReadFunction;
import hiperium.city.read.function.services.CityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class FunctionConfig {

    public static final String FUNCTION_BEAN_NAME = "findById";

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionConfig.class);

    private final CityService cityService;

    public FunctionConfig(CityService cityService) {
        this.cityService = cityService;
    }

    @Bean(FUNCTION_BEAN_NAME)
    public Function<Message<byte[]>, Mono<FunctionResponse>> findByIdFunction() {
        LOGGER.info("Creating City Data Function bean.");
        return new ReadFunction(this.cityService);
    }
}
