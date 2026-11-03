FROM maven:3.9-eclipse-temurin-21

WORKDIR /workspace

# Build the demo and its add-on dependency from the multi-module project.
COPY pom.xml ./pom.xml
COPY gantt-flow-addon/pom.xml ./gantt-flow-addon/pom.xml
COPY gantt-flow-addon/src ./gantt-flow-addon/src
COPY gantt-flow-demo/pom.xml ./gantt-flow-demo/pom.xml
COPY gantt-flow-demo/src ./gantt-flow-demo/src

RUN mvn -B -pl gantt-flow-demo -am install -DskipTests

EXPOSE 10000

CMD ["sh", "-c", "exec mvn -B -f gantt-flow-demo/pom.xml jetty:run-war -Djetty.http.host=0.0.0.0 -Djetty.http.port=${PORT:-10000}"]
