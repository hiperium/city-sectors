package hiperium.city.read.function.functions;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import hiperium.city.functions.common.responses.FunctionResponse;
import hiperium.city.functions.tests.utils.EventDeserializerTest;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.configurations.NonRoutableMessageHandler;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionApplication.class)
class RoutingFunctionTest {

    @Autowired
    private MessageConverter messageConverter;

    @Autowired
    private FunctionCatalog functionCatalog;

    @Test
    @DisplayName("Wrong function name")
    void givenRequestWithWrongFunctionName_whenInvokeLambdaFunction_thenMustLogMessage() throws IOException {
        String jsonContent = this.getJsonFromFilePath("requests/common/non-valid/wrong-function-name.json");
        assertThat(jsonContent).isNotNull();

        Message<APIGatewayProxyRequestEvent> eventMessage = EventDeserializerTest
            .deserializeRequestEvent(jsonContent, this.messageConverter);
        assertThat(eventMessage).isNotNull();

        // Capture log messages.
        LogCaptor logCaptor = LogCaptor.forClass(NonRoutableMessageHandler.class);

        // Find the corresponding function by name.
        Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> function = this.findRoutingFunction();
        assertThat(function).isNotNull();

        // Execute the function.
        Mono<FunctionResponse> response = function.apply(eventMessage);
        assertThat(response).isNull();

        // Validate log message.
        assertThat(logCaptor.getWarnLogs()).isNotEmpty();
        assertThat(logCaptor.getWarnLogs())
            .anyMatch(log -> log.contains("The following message couldn't be routed"));
    }

    private Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> findRoutingFunction() {
        return this.functionCatalog.lookup(Function.class, RoutingFunction.FUNCTION_NAME);
    }

    private String getJsonFromFilePath(String pathOfJsonDataFile) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathOfJsonDataFile);
        if (Objects.isNull(inputStream)) {
            throw new IOException("File not found: " + pathOfJsonDataFile);
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
