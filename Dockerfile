FROM eclipse-temurin:17

WORKDIR /
RUN apt-get update && apt-get install -y --no-install-recommends \
        libglvnd0 \
        libgl1 \
        libglx0 \
        libegl1 \
        libgles2 && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /src
COPY . .
RUN ./gradlew installDist

WORKDIR /
CMD ["bash", "/src/build/install/badger/bin/badger"]
