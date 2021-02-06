//회원가입 유효검사, 이미지 확장자만 업로드되도록 필터링하는 함수 불러오기
import {image_extension_filter,nickname_check,interested_category,
    checkbox_all_select, checkbox_list_select} from '/js/module/module.js';

let form_data_profile_picture;
let input_file_profile_picture;
let profile_picture_file;
// 프로필 사진 수정하여 업로드 시 기존 이미지 삭제하는 함수
function existing_image_delete(event){
    const my_upload_profile_picture = document.getElementById('my_upload_profile_picture');
    if(my_upload_profile_picture && event.target.value){
        my_upload_profile_picture.parentNode.removeChild(my_upload_profile_picture)
    }
}

//프로필 사진 업로드 시 사진 미리보는 함수
function image_preview(event){
    if(event.target.value){
        const profile_picture_form = document.getElementById('profile_picture_form')
        const reader = new FileReader();
        // 파일을 읽어들일 때 사용한다. 읽어들인 파일(여기선 이미지)을 브라우저로 출력하는 기능이다.
        reader.readAsDataURL(event.target.files[0])
        reader.onload = function(event){
            const img = document.createElement('img')
            img.setAttribute('class','rounded-circle profile_picture select_profile_picture')
            img.setAttribute('id','my_upload_profile_picture3')
            // event.target.result는 event.target이 이미지일 경우 해당 이미지의 파일 경로를 갖고 온다.
            img.setAttribute('src',event.target.result)
            profile_picture_form.replaceChild(img,profile_picture_form.firstChild)
            // 서버에 전송할 프로필 이미지를 변수에 저장
            form_data_profile_picture = new FormData();
            input_file_profile_picture = $("input[name='image']")
            profile_picture_file = input_file_profile_picture[0].files
            for (let i = 0; i < profile_picture_file.length; i++) {
                form_data_profile_picture.append('image',profile_picture_file[i])
            }
            console.log(profile_picture_file)
            console.log(form_data_profile_picture)
        }
    }
}
// 회원정보 수정하기 버튼을 클릭 시 회원정보 수정할수 있도록 하는 함수
function go_member_modify(){
    const label_InputFile = document.getElementById('label_InputFile')
    const profile_picture_form2 = document.getElementById('profile_picture_form2')
    const basic_profile_picture_btn = document.getElementById('basic_profile_picture_btn')
    const label_UserNickname = document.getElementById('label_UserNickname')
    const existing_UserNickname = document.getElementById('existing_UserNickname')
    const existing_interested_category = document.getElementById('existing_interested_category')
    const check_interested_category = document.getElementById('check_interested_category')
    label_InputFile.classList.remove('hidden')
    UserNickname.classList.remove('hidden')
    label_UserNickname.classList.remove('hidden')
    check_interested_category.classList.remove('hidden')
    cancel_member_modify.classList.remove('hidden')
    success_member_modify.classList.remove('hidden')
    basic_profile_picture_btn.classList.remove('hidden')
    profile_picture_form2.classList.add('hidden')
    existing_UserNickname.classList.add('hidden')
    existing_interested_category.classList.add('hidden')
    check_interested_category.classList.add('d-flex')
    start_member_modify.classList.add('hidden')
    leave_member.classList.add('hidden')
}
// 회원 탈퇴하기 버튼을 클릭 시 회원 탈퇴할 수 있도록 하는 함수
function withdrawal_member() {
    const withdrawal_notification =
        confirm(`회원탈퇴를 하시면 지금까지 이용하신 개인정보가 영구삭제되며 재가입 시 복구가 불가능합니다. 진행하시겠습니까?`)
    if (withdrawal_notification){
        alert('회원 탈퇴가 완료되었습니다. 지금까지 이용해주셔서 진심으로 감사드립니다.')
    }
}
// 회원정보 수정 취소 버튼을 클릭 시 회원정보 수정 전 UI로 돌아가는 함수
function back_member_modify(){
    const label_InputFile = document.getElementById('label_InputFile')
    const profile_picture_form2 = document.getElementById('profile_picture_form2')
    const basic_profile_picture_btn = document.getElementById('basic_profile_picture_btn')
    const label_UserNickname = document.getElementById('label_UserNickname')
    const existing_UserNickname = document.getElementById('existing_UserNickname')
    const existing_interested_category = document.getElementById('existing_interested_category')
    const check_interested_category = document.getElementById('check_interested_category')
    label_InputFile.classList.add('hidden')
    UserNickname.classList.add('hidden')
    label_UserNickname.classList.add('hidden')
    check_interested_category.classList.add('hidden')
    cancel_member_modify.classList.add('hidden')
    success_member_modify.classList.add('hidden')
    basic_profile_picture_btn.classList.add('hidden')
    profile_picture_form2.classList.remove('hidden')
    existing_UserNickname.classList.remove('hidden')
    existing_interested_category.classList.remove('hidden')
    check_interested_category.classList.remove('d-flex')
    start_member_modify.classList.remove('hidden')
    leave_member.classList.remove('hidden')
}
// 사용자가 기본 프로필 이미지로 수정 시 이미지를 미리볼 수 있도록 하는 함수
function create_basic_profile_picture() {
    const profile_picture_form = document.getElementById('profile_picture_form')
    profile_picture_form.innerHTML = '<img id="basic_profile_picture" src="/imgs/avatar.png" class="rounded-circle profile_picture select_profile_picture" alt="">'
}

// member_modify.html에서 수정한 회원정보 중 프로필 사진이 있을 경우 우선적으로 서버에 요청하는 함수
function request_update_profile_include_picture() {
    const param= form_data_profile_picture
    const reqPromise = fetch('/user/auth/profileimg-upload', {
        method: 'POST',
        body: param,
        headers : {}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('회원 프로필 사진 수정 완료')
            console.log(param)
            return res.json();
        }else{
            console.log('회원 프로필 사진 수정 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            request_update_profile(data)
        })
        .catch(error => {
            console.log(error)
        })
}
// member_modify.html에서 수정된 회원정보를 서버에 전송하는 함수
function request_update_profile(server_response) {
    let params;
    const check_list = document.getElementsByName('check_list');
    const basic_profile_picture = document.getElementById('basic_profile_picture')
    const category_arr = []
    for (let i = 0; i < check_list.length; i++) {
        if(check_list[i].checked == true){
            const category_obj = {}
            category_obj.category_id = `${check_list[i].classList[1]}`
            category_obj.category_name = `${check_list[i].value}`
            category_arr.push(category_obj)
        }
    }
    if(server_response){
        params = {
            profileImageDir : `${server_response.data}`,
            nickName : UserNickname.value,
            productCategories : category_arr
        }
    }
    else if(basic_profile_picture){
        params = {
            profileImageDir : null,
            nickName : UserNickname.value,
            productCategories : category_arr
        }
    }else{
        params = {
            nickName : UserNickname.value,
            productCategories : category_arr
        }
    }
    const reqPromise = fetch('/user/auth/profile-update', {
        method: 'POST',
        body: JSON.stringify(params),
        headers : {'Content-Type' : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('회원정보 최종 수정 완료')
            console.log(JSON.stringify(params))
            return res.json();
        }else{
            console.log('회원정보 최종 수정 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            // location.href = '/shop/account/member_modify'
        })
        .catch(error => {
            console.log(error)
        })
}

document.addEventListener('change',function (event) {
    if(event.target.id == 'InputFile' && image_extension_filter(event,event.target.files[0])){
        existing_image_delete(event)
        image_preview(event)
    }
})

document.addEventListener('keyup',function (event) {
    if(event.target.id == 'UserNickname'){
        nickname_check()
    }
})

document.addEventListener('click',function (event) {
    const check_list = document.getElementsByName('check_list');
    if(event.target.id == 'check_all'){interested_category();checkbox_all_select();}
    check_list.forEach((list) =>{
        list.addEventListener('click',function(){interested_category();checkbox_list_select();})
    })
    if(event.target.id == 'start_member_modify'){go_member_modify()}
    if(event.target.id == 'leave_member'){withdrawal_member()}
    if(event.target.id == 'cancel_member_modify'){back_member_modify()}
    if(event.target.id == 'basic_profile_picture_btn'){create_basic_profile_picture()}
    if(event.target.id == 'success_member_modify'){
        nickname_check();
        interested_category();
        if(nickname_check() && interested_category()){
            const my_upload_profile_picture3 = document.getElementById('my_upload_profile_picture3')
            if(my_upload_profile_picture3){request_update_profile_include_picture();}
            else{request_update_profile();}
        }
    }
})