set_env:
	@echo "Setting environment variables from .env file"
	set -a
	#use this as Makefile uses /bin/sh and source is for bash, so we need to use . instead of source
	. .env
	set +a

qa:
	./mvnw spotless:check

make apply_qa:
	./mvnw spotless:apply

up:
	docker compose up -d --force-recreate

down:
	docker compose down -v

server:
	make up
	./mvnw spring-boot:run

server_dev:
	make up
	./mvnw -Pdev spring-boot:run

test:
	./mvnw test