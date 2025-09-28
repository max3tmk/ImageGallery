.PHONY: docker idea

default: docker

docker:
	./scripts/stack_restart_docker.sh

idea:
	./scripts/stack_restart_idea.sh
