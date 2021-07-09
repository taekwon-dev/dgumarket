

# switch -> check proxy connection -> kill previous process 3 app(main, auth, gate)

function stop_app(){
    echo ">start...stop.sh"
    echo ">[stop.sh] START STOP PREVIOUS 3 APP [0/3]"
    PRE_MAIN_PID=$(ps -eo pid,lstart,cmd --sort=lstart | grep "dgumarket.*jar" | awk 'NR==2{ print $1 }')
    PRE_AUTH_PID=$(ps -eo pid,lstart,cmd --sort=lstart | grep "authorization-server.*jar" | awk 'NR==2{ print $1 }')
    PRE_GATE_PID=$(ps -eo pid,lstart,cmd --sort=lstart | grep "gateway.*jar" | awk 'NR==2{ print $1 }')


    echo ">[stop.sh] kill main app [1/3]"
    kill -15 ${PRE_MAIN_PID}
    sleep 10

    echo ">[stop.sh] kill auth app [2/3]"
    kill -15 ${PRE_AUTH_APP}
    sleep 10

    echo ">[stop.sh] kill gateway app [3/3]"
    kill -15 ${PRE_GATE_PID}
    sleep 10
    echo ">end...stop.sh"


    # 하나의 app만 남았는 지 확인합니다.
    MAIN_APP_NUM=$(ps -eo pid,lstart,cmd --sort=lstart | grep "dgumarket.*jar" | awk 'END{ print NR }')
    if [ ${MAIN_APP_NUM} == 2 ]
    then
        echo "매인앱 현재 한개의 앱만 실행 중"
    else
        echo "여러개의 앱이 실행중임. 문제 발생"
        echo "[해결방안] : main 앱이 여러개 실행되고 있습니다. 기존의 앱을 지우세요"
        # 이메일을 보냅니다.
#        echo "[problem, stop.sh : $(line ('$')) line is error]"
    fi

    AUTH_APP_NUM=$(ps -eo pid,lstart,cmd --sort=lstart | grep "authorization-server.*jar" | 'END{ print NR }')
    if [ ${AUTH_APP_NUM} == 2 ];then
        echo "인증 애플리케이션 한개의 앱만 실행 중"
    elif [ ${AUTH_APP_NUM} -gt 2 ]; then
      echo "2개 이상의 인증앱이 실행 중. 문제 발생"
        # 이메일 보내는 로직
    fi

    GATE_APP_NUM=$(ps -eo pid,lstart,cmd --sort=lstart | grep "gateway.*jar" | awk 'END{ print NR }')
    if [ ${GATE_APP_NUM} == 2 ];then
        echo "게이트웨이 애플리케이션 한개의 앱만 실행 중"
    elif [ ${GATE_APP_NUM} -gt 2 ]; then
        echo "2개 이상의 게이트 웨이앱이 실행 중. 문제 발생"
        # 이메일 보내는 로직
    fi
}

