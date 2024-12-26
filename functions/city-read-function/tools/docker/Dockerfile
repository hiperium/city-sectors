FROM public.ecr.aws/lambda/java:21-arm64

COPY target/city-read-function-aws.jar ${LAMBDA_TASK_ROOT}/lib/

ENV MAIN_CLASS="hiperium.city.read.function.FunctionApplication"

CMD [ "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest" ]
