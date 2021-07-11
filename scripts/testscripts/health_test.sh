#!/usr/bin/env bash

# $1=IDLE_PORT, $2=TARGET_APP, $3=APP실행순서( n 번째)
function check_health(){
    IDLE_PORT=$1
    WHICH_APP=$2

    echo "======================================"
    echo "[health_test.sh] [ $2 ] Health Check Start![ $3 / 3 ]"
    echo "[health_test.sh] > $2  의 TEST_PORT : $IDLE_PORT" #
    echo "[health_test.sh] > curl http://localhost:$IDLE_PORT/profile" # 테스트 어플 health 체크
    for RETRY_COUNT in {1..10}
    do
        RESPONSE=$(curl -s http://localhost:${IDLE_PORT}/profile)
        UP_COUNT=$(echo ${RESPONSE} | grep 'test' | wc -l)
        echo $UP_COUNT
        if [ $UP_COUNT -eq 1 ];
        then # $UP_COUNT >= 1 ("test" 문자열이 있는지 검증)
            echo "> [ $2 ] 의 Health check 성공[ $3 / 3 ]"
            break;
        else
            echo "> Health check의 응답을 알 수 없거나 혹은 실행 상태가 아닙니다."
            echo "> Health check: ${RESPONSE}"
        fi
        echo "> Health check 연결실패. 재시도... ( ${RETRY_COUNT} / 10 ) [ $3 / 3  ]"
        sleep 10
    done
}


check_health 5050 "main" 1

check_health 5052 "auth" 2

check_health 5051 "gate" 3