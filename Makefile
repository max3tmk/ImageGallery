.PHONY: docker idea clean

default: docker

docker:
	./scripts/stack_restart_docker.sh

idea:
	./scripts/stack_restart_idea.sh

clean:
	docker compose down -v --remove-orphans
	docker system prune -f