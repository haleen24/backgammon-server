kubectl delete deploy narde-game
echo $(minikube docker-env)
docker image rm narde-game --force
docker build -t narde-game .
kubectl apply -f ~/k8s/game-deployment.yaml