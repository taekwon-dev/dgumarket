const total_alm = document.getElementById('total_alm');
const chat_start_button = document.getElementById('chat_start_button');
const chat_form = document.getElementById('chat_form');
const chat_header = document.getElementById('chat_header')
const chat_list_form = document.getElementById('chat_list_form');
const chat_list_space = document.getElementById('chat_list_space')
const chat_search_icon = document.getElementById('chat_search_icon')
const chat_title_chat = document.getElementById('chat_title_chat');
const chat_search_form = document.getElementById('chat_search_form');
const trade_item_info = document.getElementById('trade_item_info');
const back_btn = document.getElementById('back_btn');
const empty_btn = document.getElementById('empty_btn')
const chat_room_form = document.getElementById('chat_room_form');
const chat_opponent_search = document.getElementById('chat_opponent_search');
const close_btn = document.getElementById('close_btn')
const chat_input_form1 = document.getElementById('chat_input_form1')
const chat_room_out = document.getElementById('chat_room_out');
const trade_success_form = document.getElementById('trade_success_form')
const chat_input = document.getElementById('chat_input');
const chat_submit_btn = document.getElementById('chat_submit_btn');
const chat_screen = document.getElementById('chat_screen');
const confirm_trade_success = document.getElementById('confirm_trade_success')
const chat_write_trade_comment = document.getElementById('chat_write_trade_comment')
const chat_view_trade_comment = document.getElementById('chat_view_trade_comment')
const trade_success_btn = document.getElementById('trade_success_btn')
const notification_trade_state = document.getElementById('notification_trade_state')
const notification_trade_state_text = document.getElementById('notification_trade_state_text')
const input_trade_comment = document.getElementById('input_trade_comment')
const comment_submit = document.getElementById('comment_submit')
let get_message;
let get_image;
let get_date;
let message_state;
const date = new Date();
const mm =  date.getMonth() < 9 ? `0${date.getMonth()+1}` : `${date.getMonth()+1}`;
const dd = date.getDate() < 10 ? `0${date.getDate()}` : `${date.getDate()}`
// 동적 생성된 엘리먼트 중에서 해당 클래스를 갖고 있을 경우 이벤트 바인딩하는 함수
function hasClass(elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}
// 채팅UI를 여는 함수
function chat_form_view() {
    chat_form.classList.remove('chat_close')
    chat_input_form1.classList.remove('chat_input_form_close')
    total_alm.classList.add('hidden');
    chat_start_button.classList.add('hidden');
    chat_form.classList.remove('hidden');
    if (window.matchMedia( '( min-width:280px ) and ( max-width:414px )' ).matches) {
        const body_scrollY = document.documentElement.style.getPropertyValue('--scroll-y')
        const body_scroll = document.documentElement
        body_scroll.style.position = 'fixed'
        body_scroll.style.top = `-${body_scrollY}`
    }
    chat_list_opponent_nickname_width();
    chat_list_latest_message_width();
    send_count_height();
}
// 채탕방을 여는 함수
function chat_room_view() {
    chat_list_form.classList.add('hidden');
    chat_title_chat.classList.add('hidden');
    chat_search_form.classList.add('hidden');
    trade_item_info.classList.add('d-flex')
    empty_btn.classList.add('hidden')
    trade_item_info.classList.remove('hidden');
    back_btn.classList.remove('hidden');
    back_btn.classList.add('d-flex')
    chat_room_form.classList.remove('hidden');
    chat_room_form.classList.add('chat_room_open');
    chat_input_form_width();
    const close_chat_room_form = document.getElementById('close_chat_room_form')
    if(chat_screen.className.indexOf('welcome') > -1){
        close_chat_room_form.classList.add('hidden')
    }else{close_chat_room_form.classList.remove('hidden')}
}
// 채팅방 하단UI의 너비를 조절하는 함수
function chat_input_form_width() {
    const chat_input_form1 = document.getElementById('chat_input_form1')
    if(chat_input_form1){
        chat_input_form1.style.width = (chat_form.clientWidth - 30)+'px';
    }
}
// 채팅방에서 뒤로 가는 함수
function chat_room_close(){
    chat_title_chat.classList.remove('hidden')
    chat_list_form.classList.remove('hidden')
    chat_search_form.classList.remove('hidden')
    trade_item_info.classList.remove('d-flex')
    chat_room_form.classList.remove('chat_room_open');
    empty_btn.classList.remove('hidden')
    trade_item_info.classList.add('hidden')
    back_btn.classList.add('hidden')
    back_btn.classList.remove('d-flex')
    chat_room_form.classList.add('hidden')
    chat_screen.removeAttribute('class')
    notification_trade_state.classList.add('hidden')
    init_conversation();
    chat_list_opponent_nickname_width();
    chat_list_latest_message_width();
    send_count_height();
}
// 채팅방이 하나라도 있을 경우 채팅방 없을 때 보이는 알림 이미지 안 보이게 하는 함수
function has_chat_list(){
    const chat_room_empty = document.getElementById('chat_room_empty')
    if(document.getElementsByClassName('chat_list').length > 0) {
        chat_room_empty.classList.add('hidden')
    }else{
        chat_room_empty.classList.remove('hidden')
    }
}
// 채팅방 리스트 검색하는 함수
function chat_room_search() {
    const chat_list = document.getElementsByClassName('chat_list')
    for (let i = 0; i < chat_list.length; i++) {
        const chat_opponent_nickname = chat_list[i].getElementsByClassName('chat_opponent_nickname');
        if (chat_opponent_nickname[0].innerHTML.toLowerCase().indexOf(chat_opponent_search.value.toLowerCase()) > -1) {
            chat_list[i].style.display = "flex";
        }else{
            chat_list[i].style.display = "none";
        }
    }
}
//채팅방 리스트의 닉네임 너비를 자동조절하는 함수
function chat_list_opponent_nickname_width() {
    if(chat_form.className.indexOf('hidden') == -1 && chat_room_form.className.indexOf('hidden') > -1){
        for (let i = 0; i < document.getElementsByClassName('chat_opponent_nickname').length; i++) {
            document.getElementsByClassName('chat_opponent_nickname')[i].style.maxWidth =
                (document.getElementsByClassName('chat_list_right_top')[i]
                    .clientWidth - document.getElementsByClassName('send_time')[i]
                    .clientWidth - document.getElementsByClassName('chat_list_more_view')[i].clientWidth)+'px';
        }
    }
}
//채팅방 리스트의 최근메시지의 너비를 자동조절하는 함수
function chat_list_latest_message_width() {
    if(chat_form.className.indexOf('hidden') == -1 && chat_room_form.className.indexOf('hidden') > -1){
        for (let i = 0; i < document.getElementsByClassName('chat_list_right_bottom').length; i++) {
            document.getElementsByClassName('chat_conversation')[i].style.maxWidth =
                (document.getElementsByClassName('chat_list_right_bottom')[i]
                    .clientWidth - document.getElementsByClassName('send_count')[i]
                    .clientWidth - 9) + 'px';
        }
    }
}
// 채팅 전체 폼 닫기 함수
function chat_form_close() {
    chat_form.classList.add('chat_close')
    chat_input_form1.classList.add('chat_input_form_close')
    setTimeout(function () {
        chat_form.classList.add('hidden');
        //읽지않은 채팅 메시지 갯수를 보여주는 UI는 다른 함수에서 경우의 수에 따라 view처리
        chat_start_button.classList.remove('hidden');
        if(total_alm.innerText && Number(total_alm.innerText) > 0){
            total_alm.classList.remove('hidden');
        }
        chat_room_close();
        if (window.matchMedia( '( min-width:280px ) and ( max-width:414px )' ).matches) {
            const body_scroll = document.documentElement
            const body_scrollY = body_scroll.style.top
            body_scroll.style.position = ''
            body_scroll.style.top = ''
            window.scrollTo(0, parseInt(body_scrollY || '0')*-1)
        }
    },350)
}
//채팅방 목록에서 각 채팅방의 수신 채팅 수를 알려주는 UI의 가로에 맞게 높이를 자동 조절하는 함수
function send_count_height() {
    const send_count = document.getElementsByClassName('send_count')
    if(send_count){
        for (let i = 0; i < send_count.length; i++) {
            send_count[i].style.height = (send_count[i].clientWidth)+'px';
        }
    }
}
// 채팅방의 높이를 자동조절하는 함수
function chat_screen_and_room_height() {
    chat_room_form.style.height = chat_screen.style.height =
        (chat_form.clientHeight - chat_header.clientHeight - notification_trade_state.clientHeight
            - chat_input_form1.clientHeight - 14)+'px';
}
// 채팅방에서 뒤로 갈 경우 채팅방의 대화내용 초기화하는 함수
function init_conversation() {
    while (chat_screen.hasChildNodes()){
        chat_screen.removeChild(chat_screen.firstChild)
    }
}
// 특정날짜의 요일 파싱하는 함수
function get_input_day_label(year,month,day) {
    const week = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']
    const day_of_week = week[new Date(year,month,day).getDay()];
    return day_of_week;
}
// 거래후기 작성 시 누가 누구에게 작성하는지 보여주는 함수
function from_seller_to_consumer() {
    const seller_user_id = document.getElementById('seller_user_id')
    seller_user_id.innerText = `${chat_screen.classList[3].slice(9)}`
    const nav_user_nickname = document.getElementsByClassName('nav_user_nickname')
    input_trade_comment.placeholder = `${nav_user_nickname[0].innerText}님의 진심이 담긴 한마디가 ${chat_screen.classList[3]
        .slice(9)}님에게 큰 도움이 될 수 있습니다. 또한 거래후기는 수정 삭제가 불가하므로 신중히 작성 부탁드립니다.`
}
//채팅입력 글자수 최대 2000자로 제한하는 함수
function limit_string() {
    if(chat_input.value.length > 2000){
        alert('최대 입력 글자 수는 2000자까지입니다.')
        chat_input.value = chat_input.value.slice(0,2000)
    }
}
//날짜 파싱하는 함수
function date_parsing(dt) {
    if(date.getFullYear() != dt.slice(0,4)){
        return get_date = dt.slice(0,4)+"."+ dt.slice(5,7)+"."+ dt.slice(8,10)
    }
    else if( mm+"-"+dd != dt.slice(5,10)){
        if(mm == dt.slice(5,7) && Number(dd)-1 == Number(dt.slice(8,10))){
            return get_date = '어제'
        }else{
            return get_date = dt.slice(5,7)+"월 "+ dt.slice(8,10)+"일"
        }
    }else{
        time_parsing(dt)
    }
}
//시간 파싱하는 함수
function time_parsing(tm) {
    let t = Number(tm.slice(11,13))
    if(t > 11){
        if(t > 12){t = t - 12}
        return get_date = `오후 ${t}:${tm.slice(14,16)}`
    }else{
        return get_date = `오전 ${t}:${tm.slice(14,16)}`
    }
}
// 메시지의 종류별로 내용을 파싱하는 함수
function message_parsing(msg) {
    if(msg.message_type == '0'){
        return get_message = msg.message
    }else{
        return get_message = '사진을 보냈습니다.'
    }
}
//중고물품 삭제여부에 따른 중고물품 이미지 파싱하는 함수
function image_parsing(img) {
    if(img.product_deleted == '0'){
        return get_image = img.productImgPath
    }else{
        return get_image = '/imgs/product_delete.png'
    }
}