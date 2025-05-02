label=narde-game
export -a "$(cat ../.env)"
ssh "$SERVER_HOST" "
  echo '---- cd in folder'
  cd ~/backend_server/backgammon-server/backgammon-game
  echo '---- build jar'
  sudo chmod +x gradlew
  ./gradlew clean build -x test
  echo '---- jar built'
  echo '---- deploy'
  bash ./../deploy-scripts/deploy.sh $label
  echo '---- deployed'
"
exec $SHELL