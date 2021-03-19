const input_userInfo = document.getElementById('input_userInfo')
const Resister_WebMail = document.getElementById('Resister_WebMail')

// 웹메일 입력 후 서버에 요청하는 함수
function request_certification_webmail() {
    // 웹메일을 입력하지 않은 경우
    if (Resister_WebMail.value == "") {
        alert('웹메일을 입력하지 않았습니다. 다시 한번 입력해주세요.')
    }
    // 입력한 웹메일이 DB에 없을 경우
    // else if(){
    // }
    // 입력한 웹메일이 DB의 정보와 일치한 경우 다음 페이지로 이동
    // else if(){

    // }
}

// 해당 사용자의 동국대 웹메일 입력 이벤트
input_userInfo.addEventListener('click',request_certification_webmail)