import {password_check1,password_check2} from '/js/module/module.js';
const success_newPWD = document.getElementById('success_newPWD');
const find_password = document.getElementById('Password');
const find_password_2 = document.getElementById('RepeatPassword');
// 비밀번호를 설정 했을 때 유효하지 않을 경우
find_password.addEventListener('keyup', function () {
    password_check1();
})
find_password_2.addEventListener('keyup', function () {
    password_check2();
})
// 비밀번호 변경 확인버튼을 클릭할 때 유효하지 않을 경우
success_newPWD.addEventListener('click',function () {
    password_check1();password_check2();
})
// const success_newPWD = document.getElementById('success_newPWD');
// // 비밀번호 재설정 페이지에서 완료 버튼을 클릭할 때 비밀번호가 유효할 경우 서버에 완료 요청 후 로그인 페이지로 이동
// success_newPWD.addEventListener('click',function (event) {
//     if (8 < PW.value.length < 20 && PW.value.search(/\s/) < 0 &&
//         PW.value.search(/[A-Z]/g) > 0 && PW.value.search(/[a-z]/g) > 0 &&
//         PW.value == PW2.value) {
//
//     }
//     // 비밀번호 재설정이 유효하지 않은 경우
//     else{
//
//     }
// })