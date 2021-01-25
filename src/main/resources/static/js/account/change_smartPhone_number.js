import {input_smartphone_check} from '/js/module/module.js'
// 입력한 휴대폰 번호가 현재 휴대폰 번호와 맞는지 검사하는 함수

// 인증번호 전송 버튼을 클릭 시 인증번호 입력란 보여주는 함수
document.addEventListener('click',function (event) {
    if(event.target.id == 'certification_phone_number'){input_smartphone_check()}
})