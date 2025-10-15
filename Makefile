.PHONY: docker idea clean push pull

default: docker

docker:
 ./scripts/stack_restart_docker.sh

idea:
 ./scripts/stack_restart_idea.sh

clean:
 docker compose down -v --remove-orphans
 docker system prune -f

push:
 ./scripts/pull-all.sh

pull:
 ./scripts/pull-all.sh