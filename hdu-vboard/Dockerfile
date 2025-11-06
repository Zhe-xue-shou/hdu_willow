# 基于已有的 verilator 镜像
FROM verilator/verilator:v5.016

LABEL maintainer="Zhexueshou <3311356727@qq.com>" \
      name="hdu-vboard" \
      version="0.0.1" \
      description="HDU Virtual Board Spring Boot + Verilator environment"

# 安装 Java 11, Python3, nlohmann-json
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    python3 python3-pip \
    nlohmann-json3-dev \
    git build-essential make \
    && rm -rf /var/lib/apt/lists/*

# 设置 JAVA_HOME（某些 SpringBoot 插件需要）
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:${PATH}"

# 设置工作目录
WORKDIR /workspace

# 复制 pom.xml 和源码（调试环境下建议挂载）
COPY target/hdu-vboard-0.0.1.jar /workspace/target/hdu-vboard-0.0.1.jar
COPY src/main/cpp /workspace/src/main/cpp
COPY src/main/python /workspace/src/main/python

# 设置默认的 Spring Profile 为 prod
#ENV SPRING_PROFILES_ACTIVE=prod

# 容器启动时执行
ENTRYPOINT ["java", "-jar", "/workspace/target/hdu-vboard-0.0.1.jar"]
# 默认命令行参数：如果没有其他参数，使用 prod
CMD ["--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]