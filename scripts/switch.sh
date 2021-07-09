#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
# source ${ABSDIR}/profile.sh

function switch_proxy(){
    echo "> 전환할 Port:$1"
    echo "> Port 전환"

    #  "set service_url http://127.0.0.1:${IDLE_PORT} 로 바라볼 수 있도록 합니다."
    echo "set \$service_url http://127.0.0.1:$1;" | sudo tee /etc/nginx/conf.d/service-url.inc
    echo "[switch.sh] > nginx reload"
    echo 5574 | sudo -S nginx -s reload


    # stop.sh
    echo "기존에 실행되고 있던 앱들을 모두 멈춥니다."
    if [ $1 == "8081" ];then # 8181로 물려있던 것들, prod2 로 실행한 앱 모두 지우기
        echo "kill process"
        echo $( ps -eo pid,command | grep prod2 | awk 'NR<4{ print $1 }')
        kill -15 $( ps -eo pid,command | grep prod2 | awk 'NR<4{ print $1 }')
    elif [ $1 == "8181" ];then # 8081로 물려있던 것들, prod1 로 실행한 앱 지우기
        echo "kill process"
        echo $( ps -eo pid,command | grep prod1 | awk 'NR<4{ print $1 }')
        kill -15 $(ps -eo pid,command | grep prod1 | awk 'NR<4{ print $1 }')
    fi
}


