FROM eclipse-temurin:17-jdk

# Python 3 is needed for TextTest and the texttest_rig.py helper script
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip python3-venv \
    && ln -sf /usr/bin/python3 /usr/bin/python \
    && rm -rf /var/lib/apt/lists/*

ENV PIP_BREAK_SYSTEM_PACKAGES=1
RUN pip3 install texttest

WORKDIR /app

# Copy Gradle wrapper and build config first for dependency caching
COPY Java/gradle/       Java/gradle/
COPY Java/gradlew       Java/gradlew
COPY Java/build.gradle  Java/build.gradle
RUN chmod +x Java/gradlew && cd Java && ./gradlew --no-daemon dependencies

# Copy project sources
COPY . .
RUN chmod +x Java/gradlew Java/mvnw Java/texttest_rig.py start_texttest.sh

ENV TEXTTEST_HOME=/app

# Pre-compile Java classes so the test run starts faster
RUN ./Java/gradlew -p Java --no-daemon classes testClasses

CMD ["./start_texttest.sh"]
