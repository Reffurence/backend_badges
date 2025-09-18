# Badger

Badger is a system for generating images out of templates and supplied data.
It is interfaced with via an HTTP server.

Readme is a WIP.

# Running

You may run Badger as follows:

- Run using `:run`. Simply navigate a terminal to the project directory and run:
  ```
  ./gradlew run
  ```
- Run via installation. Navigate a terminal to the project directory and run:
  ```
  ./gradlew installDist
  ```
  Then run the generated script:
  ```
  ./build/install/badger/bin/badger
  ```
- Run via docker. First build the image:
  ```
  docker build .
  ```
  Then run the generated image in a container:
  ```
  docker run -p 80:80 <image name>
  ```
- Run via docker compose. Navigate a terminal to the project directory and run:
  ```
  docker compose up
  ```
