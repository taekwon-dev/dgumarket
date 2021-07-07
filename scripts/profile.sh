#!/usr/bin/env bash

# 쉬고있는 profile을 찾습니다. 현재 애플리케이션이 어떤 포트에서 돌아가는 지를 확인한 후 어떤 설정파일로 앱을 실행할 지 결정합니다.
function find_idle_profile(){
  ARRAY=("8080" "8180" "5050") # 구동될 수 있는 모든 포트들
  for value in "${ARRAY[@]}"
  do
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${value}/profile) # 현재 auth 서버가 정상적으로 작동되고 있는 지 확인합니다.
    if [ ${RESPONSE_CODE} -ge 400 ];then # 정상이면 200, 오류가 발생한다면 400~503 사이로 발생하니 400 이상은 모두 예외로 봅니다. 만약, 현재 8082 로 작동되고 있다면 나머지 8182, 5052 는 여기서 걸러집니다.
      echo "${RESPONSE_CODE}"
    fi

    if [ ${RESPONSE_CODE} -eq 200 ];then
      CURRENT_PROFILE=$(curl -s http://localhost:${value}/profile) # 이번에는 실제로 해당포트로 요청을 날려 어떤 profile 을 사용하고 있는 지 실제로 확인합니다.
      echo "current running profile is ${CURRENT_PROFILE}"
      if [ ${CURRENT_PROFILE} == prod1 ];then # 현재 돌아가고 있는 profile 이 prod1 이라면
        if [ ${value} == 8080 ];then
          echo "8080가 실행되고 있습니다. 8180로 실행해야 합니다."
          IDLE_PROFILE=prod2 # 놀고 있는 profile 은 prod2 이고 이를 반환합니다.
        fi
      elif [ ${CURRENT_PROFILE} == prod2 ];then
        if [ ${value} == 8180 ];then
          echo "8180가 실행되고 있습니다. 8080로 실행해야 합니다."
          IDLE_PROFILE=prod1 # 놀고 있는 profile 은 prod1 이고 이를 반환합니다.
        fi
      elif [ ${CURRNET_PROFILE} == test ];then
        if [ ${value} == 5050 ];then
          echo "5050가 실행되고 있습니다"
        fi
      fi
    fi
  done
  echo "${IDLE_PROFILE}" # 쉬고 있는 profile 을 반환함
}

IDLE_PROFILE=$(find_idle_profile)
echo "${IDLE_PROFILE}"