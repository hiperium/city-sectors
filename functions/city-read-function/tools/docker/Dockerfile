#####################################################################################
############################# Stage 1: Builder Image ################################
#####################################################################################
FROM hiperium/city-builder:latest AS builder
WORKDIR /workspace

COPY target/jar-assembly.zip jar-assembly.zip

RUN  mkdir extracted && \
     unzip jar-assembly.zip -d extracted/

#####################################################################################
############################## Stage 2: Function Image ##############################
#####################################################################################
FROM public.ecr.aws/lambda/java:21-arm64

COPY --from=builder /workspace/extracted ${LAMBDA_TASK_ROOT}

ENV MAIN_CLASS="hiperium.city.read.function.FunctionApplication"

CMD [ "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest" ]
