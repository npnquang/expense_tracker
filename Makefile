set_env:
	@echo "Setting environment variables from .env file"
	set -a
	#use this as Makefile uses /bin/sh and source is for bash, so we need to use . instead of source
	. .env
	set +a

qa:
	make set_env
	./mvnw spotless:check
	./mvnw verify

make apply_qa:
	make set_env
	./mvnw spotless:apply
	./mvnw verify

up:
	make set_env
	docker compose up -d --force-recreate

down:
	docker compose down -v

server:
	make up
	./mvnw spring-boot:run

server_up:
	make up
	./mvnw -Pdev spring-boot:run