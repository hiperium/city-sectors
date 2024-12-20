#####################################################################################
############################# Stage 1: Builder Image ################################
#####################################################################################
FROM hiperium/city-builder:latest AS builder
WORKDIR /workspace

COPY tools/shell/java/bootstrap         bootstrap
COPY target/function-java-assembly.jar  function-java-assembly.jar

RUN mkdir extracted && \
    unzip function-java-assembly.jar -d extracted/

#####################################################################################
############################## Stage 2: Function Image ##############################
#####################################################################################
FROM hiperium/city-lambda:latest

COPY --from=builder /workspace/extracted ${LAMBDA_TASK_ROOT}
COPY --from=builder /workspace/bootstrap ${LAMBDA_RUNTIME_DIR}/bootstrap

ENV AGENT_OUTPUT_DIR="/tmp/native-image-configs"
ENV MAIN_CLASS="hiperium.city.read.function.FunctionApplication"

RUN rm -rf ${AGENT_OUTPUT_DIR}                && \
    mkdir -p ${AGENT_OUTPUT_DIR}              && \
    chmod +x ${LAMBDA_RUNTIME_DIR}/bootstrap

ENTRYPOINT ["sh", "/lambda-entrypoint.sh"]
CMD ["org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"]
