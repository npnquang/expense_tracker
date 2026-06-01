qa:
	./mvnw spotless:check
	./mvnw verify

up:
	docker compose up -d

down:
	docker compose down -v