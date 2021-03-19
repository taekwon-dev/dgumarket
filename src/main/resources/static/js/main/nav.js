// 회원가입 및 로그인 버튼 클릭시 해당 페이지로 이동
const button_register = document.getElementsByClassName('button_register');
const button_login = document.getElementsByClassName('button_login');
const web_items_search = document.getElementById('web_items_search')
const mobile_items_search = document.getElementById('mobile_items_search')
const nav_more_view_btn = document.getElementById('nav_more_view_btn')
const nav_few_view_btn = document.getElementById('nav_few_view_btn')
const nav_btn = document.getElementsByClassName('nav_btn')
const nav_search = document.getElementById('nav_search')
const nav_search_close = document.getElementById('nav_search_close')
const left_nav_UI = document.getElementById('left_nav_UI')
const right_nav_UI = document.getElementById('right_nav_UI')
const nav_search_form = document.getElementById('nav_search_form')
const nav_user_picture = document.getElementById('nav_user_picture')
const web_nav_user_picture = document.getElementById('web_nav_user_picture')
// 검색창 키워드 자동완성하는 함수
function search_autocomplete(){
    const keyword_list = ['전공서적','교양서적','만화책','점퍼','소설책','청바지','후드티'];
    $(".items_search").autocomplete({
        source : keyword_list,
        minLength : 1,
        autoFocus: true,
        delay: 200,
        focus : function() {
            return false;
        },
    }).autocomplete("instance")._renderItem = function (ul, item) {
        return $("<li id='keyword_set'>")
            .append("<div class='keyword_string'>"+"<i class='fas fa-search search_icon'>"+"</i>" +item.value+"</div>")
            .appendTo(ul)
    }
}

// 작은 디바이스의 navbar에서 더보기 버튼 클릭 시 x버튼으로 바꾸는 함수
function from_more_to_few(){
    nav_more_view_btn.classList.add('hidden')
    nav_few_view_btn.classList.remove('hidden')
}
// 작은 디바이스의 navbar에서 x 버튼 클릭 시 더보기 버튼으로 바꾸는 함수
function from_few_to_more() {
    nav_few_view_btn.classList.add('hidden')
    nav_more_view_btn.classList.remove('hidden')
}
// 모바일 navbar의 더보기 창에서 메뉴 클릭 시 더보기 창 닫히도록 하는 함수
function more_view_close() {
    const collapsibleNavbar = document.getElementById('collapsibleNavbar')
    collapsibleNavbar.classList.remove('show')
    from_few_to_more()
    return;
}
// 모바일 navbar에서 검색 아이콘 클릭 시 검색창 여는 함수
function start_search() {
    left_nav_UI.classList.add('hidden')
    left_nav_UI.classList.remove('d-flex')
    right_nav_UI.classList.add('hidden')
    right_nav_UI.classList.remove('d-flex')
    nav_search_form.classList.remove('hidden')
}
// 모바일 navbar의 검색창에서 뒤로가기 버튼 클릭 시 모바일 navbar 메뉴로 돌아가는 함수
function close_search() {
    nav_search_form.classList.add('hidden')
    left_nav_UI.classList.remove('hidden')
    left_nav_UI.classList.add('d-flex')
    right_nav_UI.classList.remove('hidden')
    right_nav_UI.classList.add('d-flex')
}
// 모바일 navbar에서 프로필 사진을 클릭할 때 회원가입 or 회원정보,로그인 or 로그아웃UI 보여주는 함수
function user_info_view() {
    const nav_user_info = document.getElementById('nav_user_info')
    nav_user_info.classList.toggle('hidden')
}
// 웹 navbar에서 프로필 사진을 클릭할 때 회원정보, 로그아웃UI 보여주는 함수
function web_user_info_view() {
    const web_nav_user_info = document.getElementById('web_nav_user_info')
    web_nav_user_info.classList.toggle('hidden')
}

nav_more_view_btn.addEventListener('click',from_more_to_few)

nav_few_view_btn.addEventListener('click',from_few_to_more)

web_items_search.addEventListener('keyup',search_autocomplete)

mobile_items_search.addEventListener('keyup',search_autocomplete)

nav_search.addEventListener('click',start_search)

nav_search_close.addEventListener('click',close_search)

nav_user_picture.addEventListener('click',user_info_view)

web_nav_user_picture.addEventListener('click',web_user_info_view)

for (let i = 0; i < button_register.length; i++) {
    button_register[i].addEventListener('click', function(){
        location.href = "/shop/account/webMail_certification";
        return;
    })
}

for (let i = 0; i < button_login.length; i++) {
    button_login[i].addEventListener('click', function(){
        location.href = "/shop/account/login";
        return;
    })
}

// 모바일 navbar의 더보기 창에서 메뉴 클릭 시 더보기 창 닫히도록 하는 함수
for (let i = 0; i < nav_btn.length; i++) {
    nav_btn[i].addEventListener('click',more_view_close)
}