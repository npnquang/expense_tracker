qa:
	set -a
	#use this as Makefile uses /bin/sh and source is for bash, so we need to use . instead of source
	. .env
	set +a
	./mvnw spotless:check
	./mvnw verify

make apply_qa:
	./mvnw spotless:apply
	./mvnw verify

up:
	docker compose up -d --force-recreate

down:
	docker compose down -v

start_server:
	make up
	./mvnw spring-boot:run