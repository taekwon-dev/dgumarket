#!/usr/bin/env bash

 # 해당 앱을 실행합니다.

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh
source ${ABSDIR}/health.sh

REPOSITORY=/home/ubuntu/app/step3/main
PROJECT_NAME=dgumarket

# main, auth, gateway 의 최신 빌드 jar 파일 위치 경로

MAIN_FOLDER=/home/ubuntu/app/step3/main
MAIN_IDLE_PORT=$(find_idle_port "main") # main app 이 현재 사용하지 않는 port

GATE_FOLDER=/home/ubuntu/app/step3/gateway
GATE_IDLE_PORT=$(find_idle_port "gate") # gate app 이 현재 사용하지 않는 port

AUTH_FOLDER=/home/ubuntu/app/step3/auth
AUTH_IDLE_PORT=$(find_idle_port "auth") # auth app 이 현재 사용하지 않는 port

# 현재 놀고있는 profile 에 따라 들어가는 폴더를 달리합니다.
echo "[start.sh] BUILD 파일 복사 "

IDLE_PROFILE=$(find_idle_profile "main")
echo "[start.sh] main 앱의 놀고 있는 profile : ${IDLE_PROFILE}"
if [ $IDLE_PROFILE == 'prod1' ]
then
	echo "[start.sh] cp $REPOSITORY/zip/*.jar $REPOSITORY/main-prod1/"
	cp $REPOSITORY/zip/*.jar $REPOSITORY/main-prod1/
	JAR_NAME=$(ls -tr $REPOSITORY/main-prod1/*.jar | tail -n 1)
	RUN_FOLDER=$REPOSITORY/main-prod1/
else
	echo "[start.sh] cp $RESPOSITORY/zip/*.jar $REPOSITORY/main-prod2/"
	cp $REPOSITORY/zip/*.jar $REPOSITORY/main-prod2/
	JAR_NAME=$(ls -tr $REPOSITORY/main-prod2/*.jar | tail -n 1)
	RUN_FOLDER=$REPOSITORY/main-prod2
fi

echo "> 새 애플리케이션 배포"

echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

echo ">main 앱을 profile=$IDLE_PROFILE 로 실행합니다."


## nohup_main_날짜.out
#DATE=$(date | awk '{print $1 $2 $3 $4 $5 $6 $7}')
echo "[start.sh] main 앱 실행 ---------- [ 1 / 3 ]"
nohup java -jar -Dspring.config.location=/home/ubuntu/app/step3/main/configs/application.yml -Dspring.profiles.active=$IDLE_PROFILE $REPOSITORY/main-${IDLE_PROFILE}/*.jar > $MAIN_FOLDER/main-${IDLE_PROFILE}/nohup_main_${IDLE_PROFILE}.out 2>&1 &

function start_another_app(){
	echo "현재위치 $(pwd)"
	echo "[start.sh] $1 app 이외의 다른 앱들을 실행합니다. ---------- [ 2 / 3 ]"
	if [ $1 == 'main' ];then
	   echo "run gate, auth"
	  # gate 의 idle port, profile 찾고 idle profile로 app 실행
     echo "[start.sh] search for gate_way's idle profile---------- [ 2 / 3 ]"
     IDLE_PROFILE_GATE=$(find_idle_profile "gate")
     echo "[start.sh] cp $GATE_FOLDER/*.jar $GATE_FOLDER/gateway-${IDLE_PROFILE_GATE}/"
     cp $GATE_FOLDER/*.jar $GATE_FOLDER/gateway-${IDLE_PROFILE_GATE}/
     echo "[start.sh] 현재 실행되고 있는 gate_way 와 같은 jar 파일을 profile=$IDLE_PROFILE_GATE   로 실행합니다. ---------- [ 2 / 3 ]"
     nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE_GATE -Dspring.config.location=$GATE_FOLDER/configs/application.yml $GATE_FOLDER/gateway-${IDLE_PROFILE_GATE}/*.jar > $GATE_FOLDER/gateway-${IDLE_PROFILE_GATE}/nohup_gate_${IDLE_PROFILE_GATE}.out 2>&1 &

    # auth 의 idle port, profile 찾고 idle profile로 app 실행
     echo "[start.sh] search for auth's idle profile---------- [ 3 / 3 ]"
     IDLE_PROFILE_AUTH=$(find_idle_profile "auth")
     echo "[start.sh] cp $AUTH_FOLDER/*.jar $AUTH_FOLDER/gateway-${IDLE_PROFILE_AUTH}/"
     cp $AUTH_FOLDER/*.jar $AUTH_FOLDER/auth-${IDLE_PROFILE_AUTH}/
     echo "[start.sh] 현재 실행되고 있는 auth 와 같은 jar 파일을 profile=$IDLE_PROFILE_AUTH 로    실행합니다. ---------- [ 3 / 3 ]"
     nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE_AUTH -Dspring.config.location=$AUTH_FOLDER/configs/application.yml $AUTH_FOLDER/auth-${IDLE_PROFILE_AUTH}/*.jar > $AUTH_FOLDER/auth-${IDLE_PROFILE_AUTH}/nohup_auth_${IDLE_PROFILE_AUTH}.out 2>&1 &


#     # 제대로 앱이 실행되었는 지 체크합니다.
	   check_health ${MAIN_IDLE_PORT} "main" 1
     sleep 10
	   check_health ${AUTH_IDLE_PORT} "auth" 2
     sleep 10
	   check_health ${GATE_IDLE_PORT} "gate" 3
     sleep 10
  fi
 }

start_another_app "main"

