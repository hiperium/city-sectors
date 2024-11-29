package hiperium.city.read.function.functions;

import hiperium.cities.commons.responses.FunctionResponse;
import hiperium.city.read.function.services.CityService;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class ReadFunction implements Function<Message<byte[]>, Mono<FunctionResponse>> {

    private final CityService cityService;

    public ReadFunction(CityService cityService) {
        this.cityService = cityService;
    }

    @Override
    public Mono<FunctionResponse> apply(Message<byte[]> requestMessage) {
        return null;
    }
}
