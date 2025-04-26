kubectl delete deploy "$1"
eval $(minikube docker-env)
docker image rm "$1" --force
docker build -t "$1" .
kubectl apply -f "../k8s/$1".yaml