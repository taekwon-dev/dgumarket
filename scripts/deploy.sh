#!/bin/bash

REPOSITORY=/home/ubuntu/app/step3/main
PROJECT_NAME=dgumarket

echo "> Build 파일복사"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 현재 구동 중인 애플리케이션 pid 확인"

CURRENT_PID=$(jps | grep ${PROJECT_NAME}* | awk '{print $1}')

echo "현재 구동 중인 애플리케이션 pid: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
	echo "> 현재 구동중인 애플링케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
	kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 애플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

# 추가된 주석 fix
nohup java -jar \
	-Dspring.config.location=/home/ubuntu/app/step3/main/configs/application.yml \
	$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
