
const send_authenticationMail_form = document.getElementById('send_authenticationMail_form');
const input_authorizationCode_form = document.getElementById('input_authorizationCode_form');
const requestRepeat = document.getElementById('requestRepeat');
const input_authorizationCode = document.getElementById('input_authorizationCode');
const authorizationCode = document.getElementById('authorizationCode');

// 인증번호 입력하는 폼 보여주는 함수
function open_input_certification(){
    alert('인증번호가 유저의 메일주소로 전송되었습니다.')
    input_authorizationCode_form.classList.remove('hidden');
    send_authenticationMail_form.classList.add('hidden');
}

// 인증번호 입력 후 서버에 요청하는 함수
function request_certification_number() {
    if (authorizationCode.value == "") {
        alert('인증번호를 입력하지 않았습니다. 다시 입력해주세요')
    }
    //입력한 인증번호와 인증메일의 인증번호가 서로 다를 경우
    // else if(){

    // }
    //입력한 인증번호와 인증메일의 인증번호가 서로 맞을 경우 다음 페이지로 넘기기
    // else if(){

    // }
}

send_authenticationMail.addEventListener('click', open_input_certification)

requestRepeat.addEventListener('click', function(event) {
    alert('인증메일이 재전송되었습니다.')
})

input_authorizationCode.addEventListener('click',request_certification_number)