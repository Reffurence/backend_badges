# Badger

Badger is a system for generating images out of templates and supplied data. It was written for
[Reffurence](https://github.com/Reffurence) to generate convention badges.

The core idea of Badger is to be a simple configurable HTTP server whose endpoints serve
dynamically generated images, which are built up from simple layers, such as a static image,
text, or a shape.

Badger allows generating individual images, as well as generating multiple in a batch. The
resulting outputs can be zipped or written to a PDF.

# Running

You may run Badger as follows:

- **Run using Gradle.** Simply navigate a terminal to the project directory and run:
  ```
  ./gradlew run
  ```
- **Run via installation.** Navigate a terminal to the project directory and run:
  ```
  ./gradlew installDist
  ```
  Then run the generated script:
  ```
  ./build/install/badger/bin/badger
  ```
- **Run via docker.** First build the image:
  ```
  docker build .
  ```
  Then run the generated image in a container:
  ```
  docker run -p 80:80 <image tag>
  ```
- **Run via docker compose.** Navigate a terminal to the project directory and run:
  ```
  docker compose up
  ```