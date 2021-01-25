import {password_check1,password_check2} from '/js/module/module.js';

// 비밀번호를 설정 했을 때 유효하지 않을 경우
document.addEventListener('keyup',function (event) {
    if(event.target.id == 'Password'){password_check1();}
    if(event.target.id == 'RepeatPassword'){password_check2();}
})

// 비밀번호 변경 확인버튼을 클릭할 때 유효하지 않을 경우
document.addEventListener('click',function (event) {
    if(event.target.id == 'success_chage_Pwd'){password_check1();password_check2();}
})