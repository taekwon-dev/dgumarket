#!/bin/bash

REPOSITORY=/home/ubuntu/app/test/main-test
PROJECT_NAME=dgumarket

AUTH_TEST_REPO=/home/ubuntu/app/test/auth-test
GATE_TEST_REPO=/home/ubuntu/app/test/gateway-test

echo "> 기존 테스트 jar 파일 삭제"

rm -rf $REPOSITORY/*.jar

echo "> Build 파일복사"

cp $REPOSITORY/zip/dgumarket-*-test.jar $REPOSITORY/

echo "> 새 애플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/dgumarket-*-test.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행 [ 1 / 3 ]"

# 추가된 주석 fix
# main 앱 실행
nohup java -jar \
	-Dspring.config.location=/home/ubuntu/app/test/main-test/configs/application.yml -Dspring.profiles.active=test \
	$JAR_NAME > $REPOSITORY/nohup-main-test.out 2>&1 &

# auth 앱 실행
echo "> auth-test 앱 실행 [ 2 / 3 ]"
nohup java -jar \
  -Dspring.config.location=/home/ubuntu/app/test/auth-test/configs/application.yml -Dspring.profiles.active=test \
   $AUTH_TEST_REPO/*.jar > $AUTH_TEST_REPO/nohup-auth-test.out 2>&1 &

# gate 앱 실행
echo "> gate-test 앱 실행 [ 3 / 3 ]"
nohup java -jar \
  -Dspring.config.location=/home/ubuntu/app/test/gateway-test/configs/application.yml -Dspring.profiles.active=test \
  $GATE_TEST_REPO/*.jar > $GATE_TEST_REPO/nohup-gateway-test.out 2>&1 &