#!/usr/bin/env bash

# 배포하려고하는 app 을 실행한 후 해당 앱이 제대로 돌아가는 지 확인합니다.

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
# source ${ABSDIR}/profile.sh
source ${ABSDIR}/switch.sh

# main / auth / gate 모두 체크합니다.
# IDLE_PORT_AUTH=$(find_idle_port "auth") # 해당 auth 앱 이 사용하는 포트 중 놀고 있는 포트
# IDLE_PORT_GATE=$(find_idle_port "gate") # 해당 gate 앱이 사용하는 포트 중 놀고 있는 포트
# IDLE_PORT_MAIN=$(find_idle_port "main") # 해당 app 이 사용하는 포트 중 놀고있는 포트( nginx 프록시로 연결되지 않는 포트 )

# IDLE_PORT_LIST=($IDLE_PORT_AUTH $IDLE_PORT_GATE $IDLE_PORT_MAIN)


# $1=IDLE_PORT, $2=TARGET_APP, $3=APP실행순서( 번째)
function check_health(){
    IDLE_PORT=$1
    WHICH_APP=$2

    echo "======================================"
    echo "[health.sh] [ $2 ] Health Check Start![ $3 / 3 ]"
    echo "[health.sh] > $2  의 IDLE_PORT: $IDLE_PORT" # 놀고 있는 포트입니다.
    echo "[health.sh] > curl http://localhost:$IDLE_PORT/profile" # 각 새롭게 시작한 어플리에키션이 제대로 동작하고 있는 지 확인합니다.
    for RETRY_COUNT in {1..10}
    do
        RESPONSE=$(curl -s http://localhost:${IDLE_PORT}/profile)
        UP_COUNT=$(echo ${RESPONSE} | grep 'prod' | wc -l)
        echo $UP_COUNT

        if [ $UP_COUNT -eq 1 ];
        then # $UP_COUNT >= 1 ("prod" 문자열이 있는지 검증)
            echo "> [ $2 ] 의 Health check 성공[ $3 / 3 ]"
          #  swith_proxy # -> 5/28 모두 체크되면 그때 포트 break
            if [ $2 == gate ];then
                echo "이전의 모든 앱이 정상적으로 동작하고 있음을 확인했습니다. 포트를 바꿉니다. switch proxy port"
                switch_proxy $IDLE_PORT;
            fi
            break;
        else
            echo "> Health check의 응답을 알 수 없거나 혹은 실행 상태가 아닙니다."
            echo "> Health check: ${RESPONSE}"
        fi

        if [ ${RETRY_COUNT} -eq 10 ]
        then
            echo "> Health check 실패"
            echo "> 엔진엑스에 연결하지 않고 배포를 종료합니다." # 무슨 말일까? 내가 새롭게 배포한 앱이 제대로 실행되지 않았다는 뜻! 따라서 이 과정에서 배포과정을 중단한다.
            exit 1
        fi
        echo "> Health check 연결실패. 재시도... ( ${RETRY_COUNT} / 10 ) [ $3 / 3  ]"
        sleep 10
    done

}
