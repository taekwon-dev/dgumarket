#!/bin/bash

# install 이전 beforeInstall 단계에서 이미 대상폴더가 존재하면 해당 대상폴더를 삭제하는 로직을 넣습니다.
TARGET_FOLDER=/home/ubuntu/app/test/main-test/zip

if [ -d "$TARGET_FOLDER" ]; then
    echo "$TARGET_FOLDER 가 이미 존재함. 삭제처리"
		rm -rf $TARGET_FOLDER
fi