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
MAIN_IDLE_PORT=($find_idle_port "main") # main app 이 현재 사용하지 않는 port

GATE_FOLER=/home/ubuntu/app/step3/gateway
GATE_IDLE_PORT=($find_idle_port "gate") # gate app 이 현재 사용하지 않는 port

AUTH_FOLDER=/home/ubuntu/app/step3/auth
AUTH_IDLE_PORT=($find_idle_port "auth") # auth app 이 현재 사용하지 않는 port

# echo "> Build 파일 복사"
# echo "> cp $REPOSITORY/zip/*.jar $REPOSITORY/"

# cp $REPOSITORY/zip/*.jar $REPOSITORY/

# echo "> 새 애플리케이션 배포"

# JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

# echo "> JAR Name: $JAR_NAME"

# echo "> $JAR_NAME 에 실행권한 추가"

# chmod +x $JAR_NAME

# echo "> $JAR_NAME 실행"

IDLE_PROFILE=$(find_idle_profile "main") # 실행할 profile 을 선택합니다.

echo ">main 앱을 profile=$IDLE_PROFILE 로 실행합니다."


# nohup_main_날짜.out
DATE=$(date | awk '{print $1 $2 $3 $4 $5 $6 $7}')
echo "[start.sh] main 앱 실행 ---------- [ 1 / 3 ]"
nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE -Dspring.config.location=/home/ubuntu/app/step3/main/configs/application.yml /home/ubuntu/app/step3/main/*.jar > $MAIN_FOLDER/nohup_main_${DATE}.out 2>&1 &

function start_another_app(){
	echo "현재위치 $(pwd)"
	echo "[start.sh] $1 app 이외의 다른 앱들을 실행합니다. ---------- [ 2 / 3 ]"
	if [ $1 == 'main' ];then
        	 echo "run gate, auth"
	         # gate 의 idle port, profile 찾고 idle profile로 app 실행
        	 echo "[start.sh] search for gate_way's idle profile---------- [ 2 / 3 ]"
	         IDLE_PROFILE_GATE=$(find_idle_profile "gate")
        	 echo "[start.sh] 현재 실행되고 있는 gate_way 와 같은 jar 파일을 profile=$IDLE_PROFILE_GATE   로 실행합니다. ---------- [ 2 / 3 ]"
	         nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE_GATE -Dspring.config.location=$GATE_FOLDER/configs/application.yml $GATE_FOLDER/*.jar > $GATE_FOLDER/nohup_gate_${DATE}.out 2>&1 &

        	 echo "[start.sh] search for auth's idle profile---------- [ 3 / 3 ]"
        	 IDLE_PROFILE_AUTH=$(find_idle_profile "auth")
         	echo "[start.sh] 현재 실행되고 있는 auth 와 같은 jar 파일을 profile=$IDLE_PROFILE_AUTH 로    실행합니다. ---------- [ 3 / 3 ]"
		nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE_AUTH -Dspring.config.location=$AUTH_FOLDER/configs/application.yml $AUTH_FOLDER/*.jar > $AUTH_FOLDER/nohup_auth_${DATE}.out 2>&1 &


         # 제대로 앱이 실행되었는 지 체크합니다.
	 check_health 8180 "main" 1
         sleep 10
	 check_health 8182 "auth" 2
         sleep 10
	 check_health 8181 "gate" 3
         sleep 10
       elif [ $1 == 'auth' ];then
  	     echo "run gate, main"
	     # gate 의 idle port, profile 찾고 idle profile로 app 실
	     echo "[start.sh] search for gate_way's idle profile ---------- [ 1 / 2 ]"
	     IDLE_PROFILE_GATE=$(find_idle_profile "gate")
	     echo "[start.sh] 현재 실행되고 있는 gate_way 와 같은 jar 파일을 profile=$IDLE_PROFIE_GATE로 실행합니다. ---------- [ 1 / 2 ]"
	     nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE_GATE -Dspring.config.location=$GATE_PATH/configs/application.yml $GATE_PATH/*.jar $GATE_PATH/nohup_gate_${DATE}.out 2>&1 &

         # main 의 idle port, profile 찾고 idle profile로 app 실행
     elif [ $1 == 'gate' ];then
         echo "run auth main"
         # auth 의 idle port, profile 찾고 idle profile로 app 실행
         # main 의 idle port, profile 찾고 idle profile로 app 실행
     fi
 }


start_another_app "main"
