.PHONY: docker idea services clean push pull k8s-start k8s-stop k8s-status

default: docker

docker:
	./scripts/stack_restart_docker.sh

idea:
	./scripts/stack_restart_idea.sh

services:
	./scripts/start_services_idea.sh

clean:
	docker compose down -v --remove-orphans
	docker system prune -f

push:
	/usr/local/bin/bash ./scripts/push-all.sh

pull:
	/usr/local/bin/bash ./scripts/pull-all.sh

k8s-start:
	./scripts/start-k8s.sh

k8s-stop:
	pkill -f "kubectl port-forward" || true
	minikube stop

k8s-status:
	kubectl get pods