// 동적 생성된 해당 class가 있는 태그를 클릭하였을 때 이벤트 실행되도록 해주는 함수
import {hasClass} from '/js/module/module.js';
// navbarUI component
$('nav').load('/shop/component/nav',function(){
    body_padding()
    $.getScript('https://code.jquery.com/jquery-3.5.1.js');
    $.getScript('https://code.jquery.com/ui/1.12.1/jquery-ui.js');
    $.getScript('https://unpkg.com/swiper/swiper-bundle.min.js');
    $.getScript('/js/main/nav.js');
});
//chatUI component
$('figure').load('/shop/component/chat',function () {
    return new Promise((resolve, reject) => {
        resolve($.getScript('/js/main/chat_dynamic.js'))
        reject(new Error('failed to request chatUI'))
    })
        .then( () => {return $.getScript('/js/main/chat_create.js')})
        .then( () => {return $.getScript('/webjars/sockjs-client/sockjs.min.js')})
        .then( () => {return $.getScript('/webjars/stomp-websocket/stomp.min.js')})
        .then( () => {return $.getScript('/js/main/chat_server.js')})
        .then( () => {return $.getScript('/js/main/chat_executing.js')})
        .catch( err => console.error(err))
})
// footerUI component
$('footer').load('/shop/component/footer');
// 페이지 리로드하지 않고 다른 페이지의 내용을 갖고오기
function pageLoad(event){
    event.preventDefault();
    history.pushState(null, null, event.target.getAttribute('href'));
    $('main').load(event.target.getAttribute('href')+' main>', function () {
        window.scrollTo(0, 0);
        return_item_count=0;
        request_index_item()
        request_profile()
    });
}
// 앞으로 가기 혹은 뒤로 가기 버튼 클릭 시 페이지 리로드하지 않고 해당 페이지 내용 갖고 오기
function move_prev_or_next_page(event){
    event.preventDefault();
    $('main').load(location.href+' main>',function () {
        window.scrollTo(0, 0);
        return_item_count=0;
        request_index_item()
        request_profile()
    });
}
// 카테고리 중에서 업로드된 중고물품이 없을 경우 디폴트 값을 나타내는 함수
function empty_item() {
    const empty_div = document.createElement('div')
    empty_div.setAttribute('class', 'row p-3')
    const empty_content = `
        <div class="col-12 col-sm-12 d-flex justify-content-center">
            <span id="not_upload_text" class="not_upload">아직 등록된 중고물품이 없습니다.&nbsp;</span>
            <i id="not_upload_icon" class="not_upload far fa-sad-tear"></i>
        </div>`
    empty_div.innerHTML = empty_content
    return empty_div
}
// 업로드된 중고물품을 브라우저에 렌더링하는 함수
function dynamic_creation_category_item(server_response) {
    let index_item_div;
    for (let i = 0; i < server_response.length; i++) {
        if(location.href.indexOf('/shop/main/index') > -1){
            // 카테고리 태그 동적 생성
            const index_bg_form = document.getElementById('index_bg_form')
            const category_span = document.createElement('span')
            category_span.setAttribute('class','col-3 col-sm-2 col-xl-1 badge badge-pill badge-primary index_bg')
            category_span.innerText = `${server_response[i].category_name}`
            index_bg_form.appendChild(category_span)
            // 카테고리 이름 동적 생성
            index_item_div = document.createElement('div')
            index_item_div.setAttribute('class', 'category_form row p-3')
            const index_content = `
            <div class="category_title col-8 col-sm-8 p-3 text-left"><h4>${server_response[i].category_name}</h4></div>
            <div class="col-4 col-sm-4 p-3 text-right more_view"></div>`
            index_item_div.innerHTML = index_content
        }
        if(server_response[i].productsList.length == 0){
            index_item_div.appendChild(empty_item())
        }
        for (let j = 0; j < server_response[i].productsList.length; j++) {
            const item_div = document.createElement('div')
            item_div.setAttribute('class','item_form col-6 col-sm-4 col-md-4 col-lg-3 col-xl-3')
            const content = `
              <div class="item_picture_form">
                <img src=${server_response[i].productsList[j].thumbnailImg} href="/shop/item/onePick" type="image/png" 
                class=" move_onePick item_picture" alt="">
              </div>
              <div class="item_text">
                <div><a class="move_onePick item_title" href="/shop/item/onePick">${server_response[i].productsList[j].title}</a></div>
                <div><a class="move_onePick item_price" href="/shop/item/onePick">${server_response[i].productsList[j].price}</a></div>
              </div>
              <div class="item_info_good_view">
                <!-- 중고물품 좋아요 수 -->
                <span class="good_button">좋아요</span>
                <span>${server_response[i].productsList[j].likeNums}</span>
                <span>∙</span>
                <!-- 중고물품 채팅횟수 -->
                <span>채팅횟수</span>
                <span>${server_response[i].productsList[j].chatroomNums}</span>
              </div>`
            item_div.innerHTML = content
            if(location.href.indexOf('/shop/main/index') > -1){
                index_item_div.appendChild(item_div)
            }
        }
        if(location.href.indexOf('/shop/main/index') > -1){
            const index_category_form = document.getElementById('index_category_form')
            index_category_form.appendChild(index_item_div)
        }
        if(location.href.indexOf('/shop/main/index') > -1 && server_response[i].productsList.length >= 4){
            const more_view = document.getElementsByClassName('more_view')
            more_view[i].innerHTML = `<a href="#">더 보기</a>`
        }
    }
}
//index.html에서 중고물품 데이터를 요청하는 함수
let return_item_count = 0
function request_index_item(){
    if(location.href.indexOf('/shop/main/index') > -1){
        const params= {
            lastCategoryId: return_item_count
        }
        const reqPromise = fetch('/api/product/index', {
            method: 'POST',
            body: JSON.stringify(params),
            headers : {'Content-Type' : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log('데이터 접수 성공')
                console.log(JSON.stringify(params))
                return res.json();
            }else{
                console.log('데이터 접수 실패')
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data);
                dynamic_creation_category_item(data)
                return_item_count = data[data.length-1].category_id;
            })
            .catch(error => {
                console.log(error)
            })
    }
}
// member_modify.html에서 기존 회원정보를 브라우저에 렌더링하는 함수
function dynamic_creation_profile(server_response){
    // 기존의 프로필 사진 동적 생성
    const my_upload_profile_picture = document.getElementById('my_upload_profile_picture')
    const my_upload_profile_picture2 = document.getElementById('my_upload_profile_picture2')
    if(server_response.data.profileImageDir !== null){
        my_upload_profile_picture.src = `/images/user-profile/2/280/${server_response.data.profileImageDir}`
        my_upload_profile_picture2.src = `/images/user-profile/2/280/${server_response.data.profileImageDir}`
    }
    // 기존 닉네임 동적 생성
    const existing_UserNickname = document.getElementById('existing_UserNickname')
    existing_UserNickname.innerText = `${server_response.data.nickName}`
    const UserNickname = document.getElementById('UserNickname')
    UserNickname.value = `${server_response.data.nickName}`
    // 기존 관심카테고리 동적 생성
    const existing_interested_category = document.getElementById('existing_interested_category')
    const check_list = document.getElementsByName('check_list');
    for (let i = 0; i < server_response.data.productCategories.length; i++) {
        const span_interested_category = document.createElement('span')
        span_interested_category.setAttribute('class','col-3 col-sm-2 col-xl-1 badge badge-pill ' +
            'badge-primary member_modify_bg')
        span_interested_category.innerText = `${server_response.data.productCategories[i].category_name}`
        existing_interested_category.appendChild(span_interested_category)
    }
    for (let i = 0; i < server_response.data.productCategories.length; i++) {
        for (let j = 0; j < check_list.length; j++) {
            if(server_response.data.productCategories[i].category_name == check_list[j].value){
                check_list[j].checked = true;
            }
        }
    }
}
// member_modify.html을 렌더링할 때 기존 회원정보를 요청하는 함수
function request_profile() {
    if(location.href.indexOf('/shop/account/member_modify') > -1){
        const reqPromise = fetch('/user/auth/profile', {
            method: 'GET',
            headers : {Accept : 'application/json'}
        })
        reqPromise.then(res => {
            if (res.status >= 200 && res.status < 300){
                console.log('데이터 접수 성공')
                return res.json();
            }else{
                console.log('데이터 접수 실패')
                return Promise.reject(new Error(res.status))
            }
        })
            .then(data => {
                console.log(data);
                dynamic_creation_profile(data)
            })
            .catch(error => {
                console.log(error)
            })
    }
}
// body가 네비게이션 바에 가리지 않도록 padding-top 조절하는 함수
function body_padding(){
    const body_padding_top = document.querySelector('body')
    const nav_height = document.querySelector('nav')
    body_padding_top.style.paddingTop = (nav_height.clientHeight+3)+'px';
}
// 스크롤을 움직일 경우 스크롤을 브라우저의 최상단으로 이동시키도록 하는 버튼 생성하는 함수
function move_scroll_top_btn() {
    const scroll_btn_form = document.getElementById('scroll_btn_form')
    if(!scroll_btn_form){
        const scroll_btn = document.createElement('span')
        scroll_btn.setAttribute('id','scroll_btn_form')
        scroll_btn.setAttribute('class', 'fixed-bottom container-fluid')
        const scroll_content = `
        <i id="scroll_btn" class="rounded-circle far fa-hand-point-up"></i>`
        scroll_btn.innerHTML = scroll_content
        const main = document.querySelector('main')
        main.appendChild(scroll_btn)
    }
}
// 스크롤을 브라우저의 최상단으로 이동시키도록 하는 버튼 지우는 함수
function delete_scroll_top_btn() {
    const main_tag = document.getElementById('main_tag')
    const scroll_btn_form = document.getElementById('scroll_btn_form')
    main_tag.removeChild(scroll_btn_form)
}

document.addEventListener('DOMContentLoaded',function(){
    request_index_item()
    request_profile()
})
document.addEventListener('click',function (event) {
    if (hasClass(event.target, 'site_brand')){pageLoad(event);}
    if (hasClass(event.target, 'move_user_info')){pageLoad(event);}
    if (hasClass(event.target, 'select_member_modify')){pageLoad(event)}
    if (hasClass(event.target, 'select_change_pwd')){pageLoad(event)}
    if (hasClass(event.target, 'select_change_phone_number')){pageLoad(event)}
    if (hasClass(event.target, 'move_myItem')){pageLoad(event)}
    if (hasClass(event.target, 'move_onePick')){pageLoad(event)}
    if (hasClass(event.target, 'move_ListbyCondition')){pageLoad(event)}
    if (hasClass(event.target, 'move_upload')){pageLoad(event)}
    if (hasClass(event.target, 'move_category')){pageLoad(event)}
    if(event.target.id == 'scroll_btn'){
        document.documentElement.scrollTop = 0;
    }
})
window.addEventListener('resize',body_padding)
window.addEventListener('popstate',move_prev_or_next_page)
window.addEventListener('scroll',function () {
    move_scroll_top_btn()
    if ((window.innerHeight + window.scrollY) >= document.documentElement.offsetHeight) {
        request_index_item()
    }
    if(document.documentElement.scrollTop == 0){
        delete_scroll_top_btn()
    }
})