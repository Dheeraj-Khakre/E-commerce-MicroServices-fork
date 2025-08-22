# Docker & Docker Compose Guide

## 🚀 Introduction

Docker is a platform that packages applications and all their dependencies into **containers**. These containers are lightweight, portable, and run the same across different environments.

Docker Compose is a tool that lets you define and run **multi-container** Docker applications using a simple YAML file.

---

## 🐳 Docker Concepts

* **Image** – Blueprint/template for containers (includes app code + dependencies).
* **Container** – Running instance of an image.
* **Dockerfile** – Script with instructions to build an image.
* **Registry** – Store for images (e.g., Docker Hub).

---

## 🔑 Common Docker Commands

| Command                             | Description                                               |
| ----------------------------------- | --------------------------------------------------------- |
| `docker pull <image>`               | Download an image from Docker Hub or registry             |
| `docker build -t <image_name> .`    | Build an image from a Dockerfile in the current directory |
| `docker images`                     | List all images on your system                            |
| `docker run [options] <image>`      | Run a new container from an image                         |
| `docker ps`                         | List running containers                                   |
| `docker ps -a`                      | List all containers (including stopped)                   |
| `docker exec -it <container> <cmd>` | Execute a command inside a running container              |
| `docker logs -f <container>`        | Show and follow logs of a container                       |
| `docker stop <container>`           | Stop a running container                                  |
| `docker start <container>`          | Start a stopped container                                 |
| `docker rm <container>`             | Remove a container                                        |
| `docker rmi <image>`                | Remove an image                                           |
| `docker network ls`                 | List networks                                             |
| `docker volume ls`                  | List volumes                                              |

---

## 📦 Docker Compose Concepts

* **Service** – Each container (e.g., app, db, redis) defined in YAML.
* **Network** – All services in a compose file share a network and can reach each other by name.
* **Volume** – Persistent data storage across container restarts.

---

## 🔑 Common Docker Compose Commands

| Command                               | Description                                 |
| ------------------------------------- | ------------------------------------------- |
| `docker compose up`                   | Create & start all services                 |
| `docker compose up -d`                | Start services in detached mode             |
| `docker compose down`                 | Stop & remove containers, networks, volumes |
| `docker compose build`                | Build or rebuild service images             |
| `docker compose pull`                 | Pull images defined in the compose file     |
| `docker compose logs`                 | Show logs of all services                   |
| `docker compose ps`                   | List running services                       |
| `docker compose exec <service> <cmd>` | Run a command inside a service container    |

---

## 🛠 Example: Spring Boot + MySQL + Kafka + MongoDB + Redis + Zipkin

Below is a sample `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  app:
    build: .
    image: my-springboot-app:latest
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      kafka:
        condition: service_started
      mongodb:
        condition: service_started
      redis:
        condition: service_started
      zipkin:
        condition: service_started

  mysql:
    image: mysql:8
    environment:
      - MYSQL_DATABASE=appdb
      - MYSQL_ROOT_PASSWORD=rootpw
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.1
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.0.1
    environment:
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"

  mongodb:
    image: mongo:5.0
    ports:
      - "27017:27017"

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  zipkin:
    image: openzipkin/zipkin
    ports:
      - "9411:9411"
```

### ▶️ Run the application

```bash
docker compose up
```

### ⏹ Stop everything

```bash
docker compose down
```

---

## ✅ Summary

* Docker = container platform
* Docker Compose = multi-container orchestration tool
* You can manage your full app stack with just a few commands!
