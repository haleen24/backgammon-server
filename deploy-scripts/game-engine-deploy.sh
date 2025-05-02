label=game-engine
export -a "$(cat ../.env)"
ssh "$SERVER_HOST" "
  echo '---- cd in folder'
  cd ~/backend_server/backgammon-server/game-engine
  echo '---- build docker image'
  docker build -t $label .
  echo '---- docker image built'
  echo '---- deploy'
  bash ./../deploy-scripts/deploy.sh $label
  echo '---- deployed'
"
exec $SHELL