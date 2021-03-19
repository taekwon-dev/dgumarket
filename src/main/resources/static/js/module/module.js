// 동적 생성된 해당 class가 있는 태그를 클릭하였을 때 이벤트 실행되도록 해주는 함수
export function hasClass(elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}
// 현재 위치하는 url을 확인하는 함수
export function url_point(my_url) {
    const url = location.href.split('/')
    return url.indexOf(my_url) > -1
}
// 닉네임 유효검사하는 함수
export function nickname_check() {
    const nickname = document.getElementById('UserNickname');
    const wrong_nickname = document.getElementById('wrong_nickname');
    const correct_nickname = document.getElementById('correct_nickname')
    if (nickname.value.length<3 || nickname.value.length>20) {
        wrong_nickname.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
        +"<span>"+" 닉네임은 3~20자까지 가능합니다."+"</span>";
        correct_nickname.innerText = "";
        return false;
    }else if (-1 < nickname.value.search(/\s/)) {
        wrong_nickname.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 닉네임은 공백 없이 입력해주세요."+"</span>";
        correct_nickname.innerText = "";
        return false;
    }else{
        wrong_nickname.innerText = "";
        correct_nickname.innerHTML = "<i class=\"fas fa-check-circle\"></i>"
            +"<span>"+" 사용가능한 닉네임입니다."+"</span>";
        return true;
    }
}

// '비밀번호 입력' 유효검사하는 함수
export function password_check1() {
    const PW = document.getElementById('Password');
    const wrong_password = document.getElementById('wrong_password');
    const correct_password = document.getElementById('correct_password');

    if (PW.value.length<8 || PW.value.length>20) {
        wrong_password.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 비밀번호는 8~20자까지 입력 가능합니다."+"</span>";
        correct_password.innerText = "";
        return false;
    } else if (-1 < PW.value.search(/\s/)) {
        wrong_password.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 비밀번호는 공백 없이 입력해주세요"+"</span>";
        correct_password.innerText = "";
        return false;
    } else if ( PW.value.search(/[A-Z]/g) < 0 || PW.value.search(/[a-z]/g) < 0) {
        wrong_password.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 비밀번호는 대소문자 포함해서 입력해주세요"+"</span>";
        correct_password.innerText = "";
        return false;
    }else{
        wrong_password.innerText = "";
        correct_password.innerHTML = "<i class=\"fas fa-check-circle\"></i>"
            +"<span>"+" 사용가능한 비밀번호입니다."+"</span>";
        return true;
    }
}
// '비밀번호 확인' 유효검사하는 함수
export function password_check2() {
    const PW = document.getElementById('Password');
    const PW2 = document.getElementById('RepeatPassword');
    const wrong_repeat_password = document.getElementById('wrong_repeat_password');
    const correct_repeat_password = document.getElementById('correct_repeat_password');
    if(PW2.value == "") {
        wrong_repeat_password.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            + "<span>" + " 비밀번호를 재입력해주세요" + "</span>";
        correct_repeat_password.innerText = "";
        return false;
    }
    else if(PW.value !== PW2.value){
        wrong_repeat_password.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 동일한 비밀번호를 입력해주세요"+"</span>";
        correct_repeat_password.innerText = "";
        return false;
    }
    else{
        wrong_repeat_password.innerText = "";
        correct_repeat_password.innerHTML = "<i class=\"fas fa-check-circle\"></i>"
            +"<span>"+" 비밀번호가 서로 일치합니다."+"</span>";
        return true;
    }
}
// 관심카테고리 선택 유효검사 하는 함수
export function interested_category() {
    const check_all = document.getElementById('check_all');
    const check_list = document.getElementsByName('check_list');
    const wrong_check = document.getElementById('wrong_check');
    const correct_check = document.getElementById('correct_check');
    let category_check = 0;
    for (let i=0; i < check_list.length; i++) {
        if (check_list[i].checked == true) {
            category_check += 1;
        }
    }
    if(check_all.checked == false && category_check < 3){
        wrong_check.innerHTML = "<i class=\"fas fa-times-circle\"></i>"
            +"<span>"+" 최소한 3개 이상 체크해주세요"+"</span>";
        correct_check.innerText = "";
        return false;
    }else{
        wrong_check.innerText = "";
        correct_check.innerHTML = "<i class=\"fas fa-check-circle\"></i>"
            +"<span>"+" 정상적으로 체크 완료하셨습니다."+"</span>";
        return true;
    }
}

// 프로필 사진 업로드 시 이미지 확장자만을 필터링하는 함수
export function image_extension_filter(event,img) {
    const pathpoint = img.name.lastIndexOf('.');
    const filepoint = img.name.substring(pathpoint+1);
    const filetype = filepoint.toLowerCase();
    if (filetype !== 'jpg' && filetype !== 'gif' && filetype !== 'png' &&
        filetype !== 'jpeg') {
        alert('bmp를 제외한 이미지 파일만 업로드할 수 있습니다.');
        event.target.value = ""
        return false;
    }else{
        return true;
    }
}

//전체 선택 체크박스 클릭 시 모든 체크박스 체크 or 해제
export function checkbox_all_select() {
    const check_all = document.getElementById('check_all');
    const check_list = document.getElementsByName('check_list');
    for (let i = 0; i < check_list.length; i++) {
        check_list[i].checked = check_all.checked;
    }
}

//체크박스 하나라도 체크가 안 돼 있을 경우 전체동의 체크박스 해제 and 모든 체크박스 클릭시 전체동의 체크 박스 체크
export function checkbox_list_select() {
    const check_all = document.getElementById('check_all');
    const check_list = document.getElementsByName('check_list');
    for (let j = 0; j < check_list.length; j++) {
        if (check_list[j].checked == false) {
            check_all.checked = false;
            return;
        }
    }
    check_all.checked = true;
}

// 휴대폰 번호를 정상적으로 입력 완료할 경우 인증번호란 보여주는 함수
export function input_smartphone_check(){
    const notification_text = document.getElementById('notification_text')
    const user_phone_number = document.getElementById('user_phone_number')
    const input_certification_number_form = document.getElementById('input_certification_number_form')
    // 핸드폰 번호를 입력하지 않고 인증번호 전송 버튼을 눌렀을 경우
    if (user_phone_number.value =="") {
        alert('변경할 휴대폰 번호를 입력하지 않았습니다. 다시 입력해주세요.')
        return false;
    }
    else if(user_phone_number.value.slice(0,2) !== "01" || user_phone_number.value.length < 10){
        alert('올바른 휴대폰 번호 입력방식이 아닙니다. 다시 입력해주세요.')
        return false;
    }
    // 핸드폰 번호를 정상적으로 입력 후 인증번호 전송 버튼을 눌렀을 경우
    else{
        if(notification_text){
            notification_text.classList.remove('hidden')
        }
        input_certification_number_form.classList.remove('hidden');
    }
}