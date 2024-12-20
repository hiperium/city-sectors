package hiperium.city.read.function;

import hiperium.city.functions.common.utils.FunctionsUtil;
import hiperium.city.functions.tests.utils.DynamoDbTableUtil;
import hiperium.city.read.function.common.TestContainersBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.adapter.aws.FunctionInvoker;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = FunctionApplication.class)
public class FunctionApplicationTest extends TestContainersBase {

    public static final String MAIN_CLASS_PROPERTY = "MAIN_CLASS";

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${city.table}")
    private String tableName;

    @BeforeAll
    public static void setup() {
        System.setProperty(MAIN_CLASS_PROPERTY, FunctionApplication.class.getName());
    }

    @BeforeEach
    void init() {
        DynamoDbTableUtil.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @ParameterizedTest
    @DisplayName("Valid requests")
    @ValueSource(strings = {
        "requests/city/valid/find-city-by-id-request.json",
        //"requests/city-sectors/valid/find-sectors-by-city-id-request.json",
    })
    public void givenValidRequest_whenInvokeFunctionAdapter_thenReturnValidResponse(String jsonFilePath) throws Exception {

        FunctionInvoker invoker = new FunctionInvoker();

        InputStream requestInputStream = this.getInputStreamFromFilePath(jsonFilePath);
        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
        invoker.handleRequest(requestInputStream, responseOutputStream, null);

        Map response = FunctionsUtil.OBJECT_MAPPER.readValue(responseOutputStream.toByteArray(), Map.class);
        assertThat(response.get("body").toString()).isEqualTo("[{\"name\":\"JIM LAHEY\"},{\"name\":\"RICKY\"}]");
    }

    @AfterAll
    public static void cleanup() {
        System.clearProperty(MAIN_CLASS_PROPERTY);
    }

    private InputStream getInputStreamFromFilePath(String pathOfJsonDataFile) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathOfJsonDataFile);
        if (Objects.isNull(inputStream)) {
            throw new IOException("File not found: " + pathOfJsonDataFile);
        }
        return inputStream;
    }
}
