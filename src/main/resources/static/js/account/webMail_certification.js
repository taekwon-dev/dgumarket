import {checkbox_all_select, checkbox_list_select}
from '/js/module/module.js';
const check_all = document.getElementById('check_all');
const check_list = document.getElementsByName('check_list');
const next_sign_up = document.getElementById('next_sign_up')
const Resister_WebMail = document.getElementById('Resister_WebMail')
const termsOfUse = document.getElementById('termsOfUse')
const privacyPolicy = document.getElementById('privacyPolicy')
const go_login = document.getElementById('go_login');

// 서버에 회원가입 시 해당 웹메일 가입 가능여부 요청하는 함수
function request_signup_webmail() {
    if (Resister_WebMail.value == ""){
        alert('웹메일을 입력해주세용')
    }
    else if(termsOfUse.checked != true || privacyPolicy.checked != true){
        alert('이용약관(필수)에 전부 동의하셔야 다음 단계로 진행할 수 있습니당')
    }
    else {
        const param= {
            webMail:Resister_WebMail.value+'@dongguk.edu',
        }
        const reqPromise = fetch('/api/auth/check/webmail', {
            method: 'POST',
            body: JSON.stringify(param),
            headers : {'Content-Type' : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log('웹메일 중복체크 성공')
                console.log(JSON.stringify(param))
                // return res.json();
            }else{
                console.log('웹메일 중복체크 중 오류 발생')
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data);
                if (data == "false"){
                    alert('해당 웹메일에 다음 단계로 진행할 수 있는 url을 남겨놨습니다. 확인 부탁드립니다.')

                    // 해당 이메일로 회원가입 2단계로 갈 수 있는 url 제공
                    const resPromise = fetch('/api/auth/send/webmail', {
                        method: 'POST',
                        body: JSON.stringify(param),
                        headers : {'Content-Type' : 'application/json'}
                    })
                    resPromise.then(res => {
                        if (res.status >= 200 && res.status < 300){
                            console.log('웹메일 전송 성공')
                            console.log(JSON.stringify(param))
                            // return res.json();
                        }else{
                            console.log('웹메일 전송 중 오류 발생')
                            return Promise.reject(new Error(res.status))
                        }
                    })
                        .then(data =>{
                            console.log(data)
                        })
                        .catch(error => {
                            console.log(error)
                        })
                }
                else if(data == "true"){
                    alert('이미 회원등록이 되어 있는 웹메일입니다. 다시 진행하시려면 다른 웹메일을 입력부탁드립니다.')
                }
            })
            .catch(error => {
                console.log(error)
            })
    }
}

check_all.addEventListener('click',checkbox_all_select);

for (let i = 0; i < check_list.length; i++) {
    check_list[i].addEventListener('click',checkbox_list_select);
}

next_sign_up.addEventListener('click',request_signup_webmail)

//로그인 페이지로 이동
go_login.addEventListener('click',function() {
    location.href = "/shop/account/login";
})