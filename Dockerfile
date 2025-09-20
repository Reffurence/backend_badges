FROM eclipse-temurin:17

# Install OpenGL-related packages, Skiko needs these despite the fact that it uses CPU rendering
WORKDIR /
RUN apt-get update && apt-get install -y --no-install-recommends \
        libglvnd0 \
        libgl1 \
        libglx0 \
        libegl1 \
        libgles2 && \
    rm -rf /var/lib/apt/lists/*

# Copy and build
WORKDIR /src
COPY . .
RUN chmod +x ./gradlew     # Make sure we can execute gradlew
RUN ./gradlew installDist  # Install

# Run installed
WORKDIR /
CMD ["bash", "/src/build/install/badger/bin/badger"]
