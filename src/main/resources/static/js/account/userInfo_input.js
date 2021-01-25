//회원가입 유효검사 함수 불러오기
import {nickname_check,password_check1,password_check2,interested_category,checkbox_all_select,
    checkbox_list_select} from '/js/module/module.js';
const nickname = document.getElementById('UserNickname');
const sign_up_password1 = document.getElementById('Password');
const sign_up_password2 = document.getElementById('RepeatPassword');
const check_all = document.getElementById('check_all');
const check_list = document.getElementsByName('check_list');
const success_member_produce = document.getElementById('success_member_produce');

// 닉네임, 비밀번호를 설정 했을 때 유효하지 않을 경우
nickname.addEventListener('keyup',nickname_check)
// '비밀번호'에 입력할 때 유효하지 않을 경우
sign_up_password1.addEventListener('keyup',password_check1)
// '비밀번호 재입력'에 입력할 때 유효하지 않을 경우
sign_up_password2.addEventListener('keyup',password_check2)

// 관심카테고리를 선택했을 때 유효하지 않을 경우
check_all.addEventListener('click',interested_category)

check_list.forEach((list) =>{
    list.addEventListener('click',interested_category)
})

// 회원가입 완료 버튼을 클릭할 때 유효하지 않을 경우
success_member_produce.addEventListener('click', function () {
    nickname_check()
    password_check1()
    password_check2()
    interested_category()
})

//회원가입 닉네임 중복검사(보류)

//전체 선택 체크박스 클릭 시 모든 체크박스 체크 or 해제
check_all.addEventListener('click',checkbox_all_select);

//체크박스 하나라도 체크가 안 돼 있을 경우 전체동의 체크박스 해제 and 모든 체크박스 클릭시 전체동의 체크 박스 체크
for (let i = 0; i < check_list.length; i++) {
    check_list[i].addEventListener('click',checkbox_list_select);
}