#!/usr/bin/env bash

# 쉬고있는 profile을 찾습니다. 현재 애플리케이션이 어떤 포트에서 돌아가는 지를 확인한 후 어떤 설정파일로 앱을 실행할 지 결정합니다.
# 파라미터로 구체적인 앱의 종류를 정합니다.
function find_idle_profile(){
	if [ $1 == "gate" ];then
		ARRAY=("8081" "8181") # GATE APP 이 이용가능한 모든 포트들
	elif [ $1 == "main" ];then 
		ARRAY=("8080" "8180") # MAIN APP이 이용가능한 모든 포트들
	elif [ $1 == "auth" ];then
		ARRAY=("8082" "8182") # AUTH APP이 이용가능한 모든 포트들
	fi

	for value in "${ARRAY[@]}" # 어떤 포트들을 사용하고 있는 지를 확인합니다. -> 이를통해 반대로 어떤 포트가 사용하고 있지 않은 지를 확인함
	do
		RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${value}/profile) # 해당 포트번호 앱이 동작하고 있는 지 확인한다. ( 또한 현재 어떤 profile 을 사용하고 있는 지 확인합니다. )
#		if [ ${RESPONSE_CODE} -ge 400 ];then # 정상이면 200, 오류가 발생한다면 400~503 사이로 발생하니 400 이상은 모두 예외로 처리됨. 만약, 현재 8082 로 작동되고 있다면 나머지 8182는 여기서 걸러진다.
#				echo "${RESPONSE_CODE}"
#		fi

		if [ ${RESPONSE_CODE} -eq 200 ];then 
			CURRENT_PROFILE=$(curl -s http://localhost:${value}/profile) # 이번에는 실제로 해당포트로 요청을 날려 어떤 profile 을 사용하고 있는 지 확인합니다.
			if [ ${CURRENT_PROFILE} == prod1 ];then # 현재 돌아가고 있는 앱의 profile이 prod1 이라면 -> prod2 profile 로 앱을 실행해야한다.
				IDLE_PROFILE=prod2 # 놀고 있는 profile 은 prod2 이고 이를 반환한다.
			      	break
			elif [ ${CURRENT_PROFILE} == prod2 ];then
				IDLE_PROFILE=prod1 # 놀고 있는 profile 은 prod1 이고 이를 반환한다.
				break
			fi
		fi
	done
	echo "${IDLE_PROFILE}" # 쉬고 있는 profile 을 반환함
}

# 쉬고 있는 profile의 port 찾기
function find_idle_port(){
	IDLE_PROFILE=$(find_idle_profile $1)
	if [ ${IDLE_PROFILE} == prod1 ];then # 현재 놀고 있는 profile 을 찾는다. 만약 prod1 이고
		if [ $1 == main ];then # main app 일 경우 놀고있는 포트 (사용할 포트)는 8080
			echo "8080"
		elif [ $1 == gate ];then # gate app 일 경우 놀있는 포트(사용할 포트)는 8081
			echo "8081"
		elif [ $1 == auth ];then # auth app 일 경우 놀고있는 포트(사용할 포트)는 8082
			echo "8082"
		fi
	elif [ ${IDLE_PROFILE} == prod2 ];then # 현재 놀고 있는 profile 을 찾는다. 만약 prod2 이고
		if [ $1 == main ];then # main app 일 경우 놀고있는 포트(사용할 포트)는 8180
			echo "8180"
		elif [ $1 == gate ];then # gate app 일 경우 놀고있는 포트(사용할 포트)는 8181
			echo "8181"
		elif [ $1 == auth ];then # auth app 일 경우 놀고있는 포트(사용할 포트)는 8182
			echo "8182"
		fi
	fi
}