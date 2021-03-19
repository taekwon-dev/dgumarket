const go_login = document.getElementById('go_login');
const Resister_WebMail = document.getElementById('Resister_WebMail');
const password = document.getElementById('password');
const go_register = document.getElementById('go_register');

// 서버에 로그인 요청하는 함수
function request_login() {
    // 웹메일 혹은 비밀번호 중 하나라도 입력을 안 할 경우
    if (Resister_WebMail.value == "" || password.value == ""){
        alert('웹메일 혹은 비밀번호가 입력되지 않았습니다. 다시 입력하세요.')
    }
    // 웹메일과 비밀번호 정보를 입력했을 경우
    else{
        const params= {
            webMail:Resister_WebMail.value+'@dongguk.edu',
            password:password.value
        }
        const reqPromise = fetch('/user/login', {
            method: 'POST',
            body: JSON.stringify(params),
            headers : {'Content-Type' : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log('로그인 성공')
                console.log(JSON.stringify(params))
                // return res.json();
            }else{
                console.log('회원가입이 되어 있지 않은 계정입니다.')
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data);
                location.href = "/shop/main/index"

            })
            .catch(error => {
                console.log(error)
            })
    }
}

go_login.addEventListener('click',request_login)

go_register.addEventListener('click',function(){
    // 회원가입 1단계 페이지로 이동
    location.href = "/shop/account/webMail_certification";
});
