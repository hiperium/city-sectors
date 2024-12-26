package hiperium.city.read.function.common;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import hiperium.city.functions.common.loggers.HiperiumLogger;

import java.nio.charset.StandardCharsets;

public class TestContext implements Context {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(Context.class);

    @Override
    public String getAwsRequestId() {
        return "test-aws-request-id";
    }

    @Override
    public String getLogGroupName() {
        return "test-log-group";
    }

    @Override
    public String getLogStreamName() {
        return "test-log-stream";
    }

    @Override
    public String getFunctionName() {
        return "test-function-name";
    }

    @Override
    public String getFunctionVersion() {
        return "1.0";
    }

    @Override
    public String getInvokedFunctionArn() {
        return "arn:aws:lambda:us-east-1:123456789012:function:test-function";
    }

    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        return null;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return 300000;
    }

    @Override
    public int getMemoryLimitInMB() {
        return 512;
    }

    @Override
    public LambdaLogger getLogger() {
        return new LambdaLogger() {
            @Override
            public void log(String message) {
                LOGGER.debug(message);
            }
            @Override
            public void log(byte[] message) {
                LOGGER.debug(new String(message, StandardCharsets.UTF_8));
            }
            @Override
            public void log(String message, LogLevel logLevel) {
                LOGGER.debug(message);
            }
            @Override
            public void log(byte[] message, LogLevel logLevel) {
                LOGGER.debug(new String(message, StandardCharsets.UTF_8));
            }
        };
    }

}
