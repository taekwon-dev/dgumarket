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
const close_chat_room_form = document.getElementById('close_chat_room_form')
const trade_success_form = document.getElementById('trade_success_form')
const chat_input = document.getElementById('chat_input');
const chat_submit_btn = document.getElementById('chat_submit_btn');
const send_file_btn = document.getElementById('send_file_btn')
const send_file_text = document.getElementById('send_file_text')
const input_send_file = document.getElementById('send-file')
const notification_msg_form = document.getElementById('notification_msg_form')
const notification_msg_btn = document.getElementById('notification_msg_btn')
const notification_msg_text = document.getElementById('notification_msg_text')
const notification_msg_icon = document.getElementById('notification_msg_icon')
const block_form = document.getElementById('block_form')
const block_btn = document.getElementById('block_btn')
const block_text = document.getElementById('block_text')
const block_icon = document.getElementById('block_icon')
const report_form = document.getElementById('report_form')
const report_submit = document.getElementById('report_submit')
const input_report = document.getElementById('input_report')
const chat_screen = document.getElementById('chat_screen');
const confirm_trade_success = document.getElementById('confirm_trade_success')
const chat_write_trade_comment = document.getElementById('chat_write_trade_comment')
const chat_view_trade_comment = document.getElementById('chat_view_trade_comment')
const trade_success_btn = document.getElementById('trade_success_btn')
const trade_success_text = document.getElementById('trade_success_text')
const notification_trade_state = document.getElementById('notification_trade_state')
const notification_trade_state_text = document.getElementById('notification_trade_state_text')
const input_trade_comment = document.getElementById('input_trade_comment')
const comment_submit = document.getElementById('comment_submit')
const trade_state = document.getElementById('trade_state')
let get_message;
let get_image;
let get_date;
let message_state;
const date = new Date();
const mm =  date.getMonth() < 9 ? `0${date.getMonth()+1}` : `${date.getMonth()+1}`;
const dd = date.getDate() < 10 ? `0${date.getDate()}` : `${date.getDate()}`
// 홈페이지 접속 후 채팅메시지 알림 허용을 클라이언트에 요청하는 함수
function permission_notification_message() {
    if(!("Notification" in window)){
        alert('ios의 경우 채팅 메시지 알림을 지원하지 않는 환경입니다.')
    }
    Notification.requestPermission( result => {
        if(result == 'denied'){
            console.log("전체 채팅 메시지 알림 차단 상태")
        }else{
            console.log('전체 채팅 메시지 알림 허용 상태')
        }
    });
}
// 채팅메시지 알림을 띄우는 함수
function show_notification_message(msg) {
    const pop_message = new Notification(`동대방네 : ${msg.chatMessageUserDto.nickName}님의 메시지`,{
        body : msg.message,
        icon : msg.chatRoomProductDto.productImgPath,
        tag : msg.roomId
    })
    console.log('알림이 왔습니다.')
}
// 로컬 스토리지에 채팅방의 알림설정 정보가 저장되어 있지 않을 경우 알림설정을 on으로 설정하여 새롭게 저장하는 함수
function localStorage_notification_message(res) {
    if(localStorage.getItem(`notification_list_no${web_items_search.value}`) == null){
        console.log('key가 없다.')
        const notification_list = [];
        localStorage_new_notification_message(res, notification_list)
        localStorage.setItem(`notification_list_no${web_items_search.value}`,JSON.stringify(notification_list))
    }
    else{
        const notification_list = JSON.parse(localStorage.getItem(`notification_list_no${web_items_search.value}`))
        const mode = notification_list.filter( li => li.room_id == res.roomId)
        if(mode.length == 0){
            console.log('key가 존재하지만 해당 채팅방의 알림설정 정보가 없다.')
            localStorage_new_notification_message(res, notification_list)
            localStorage.setItem(`notification_list_no${web_items_search.value}`,JSON.stringify(notification_list))
        }
    }
}
// 새롭게 생성된 채팅방의 알림설정을 on으로 하여 로컬스토리지에 저장하는 함수
function localStorage_new_notification_message(res, notification_list) {
    const notification_object = {}
    notification_object.room_id = res.roomId;
    notification_object.notification_mode = 'ON'
    notification_list.push(notification_object)
    console.log(notification_list)
}
// 채팅방 목록 렌더링 시 각 채팅방의 메시지 알림 설정 여부에 따라 알림버튼의 이름을 파싱해주는 함수
function chat_list_notification_message(notif, i) {
    const notification_list = JSON.parse(localStorage.getItem(`notification_list_no${web_items_search.value}`))
    const mode = notification_list.filter( li => li.room_id == notif.roomId)[0].notification_mode
    console.log(mode)
    const chat_notification_message = document.getElementsByClassName('chat_notification_message')
    if(mode == "ON"){
        chat_notification_message[i].innerText = '알림끄기'
    }
    else if(mode == "OFF"){
        chat_notification_message[i].innerText = '알림켜기'
    }
}
// 해당 채팅방 혹은 채팅방목록에서 알림 설정을 할 경우 로컬스토리지에도 알맞게 반영되도록 하는 함수
function switch_notification_message(room_id) {
    const notification_list = JSON.parse(localStorage.getItem(`notification_list_no${web_items_search.value}`))
    const mode = notification_list.filter( li => li.room_id == room_id)[0].notification_mode
    console.log(mode)
    switch (mode) {
        case 'ON':
            for (let i = 0; i < notification_list.length; i++) {
                if(notification_list[i].room_id == room_id){
                    notification_list[i].notification_mode = "OFF"
                    break;
                }
            }
            localStorage.setItem(`notification_list_no${web_items_search.value}`,JSON.stringify(notification_list))
            break;
        case 'OFF':
            for (let i = 0; i < notification_list.length; i++) {
                if(notification_list[i].room_id == room_id){
                    notification_list[i].notification_mode = "ON"
                    break;
                }
            }
            localStorage.setItem(`notification_list_no${web_items_search.value}`,JSON.stringify(notification_list))
            break;
    }
}
// 메시지가 왔을 때 해당 메시지의 채팅방 알림설정 여부 확인하는 함수
function check_notification_message(frame) {
    const notification_list = JSON.parse(localStorage.getItem(`notification_list_no${web_items_search.value}`))
    const mode = notification_list.filter( li => li.room_id == frame.roomId)[0].notification_mode
    console.log(mode)
    if(mode == "ON"){
        return 'ON';
    }
    else if(mode == "OFF"){
        return 'OFF';
    }
}
// 채팅UI를 여는 함수
function chat_form_view() {
    chat_form.classList.remove('chat_close')
    chat_input_form1.classList.remove('chat_input_form_close')
    chat_start_button.classList.add('hidden');
    chat_form.classList.add('chat_open')
    chat_input_form1.classList.add('chat_input_form_open')
    chat_form.classList.remove('hidden');
    if (window.matchMedia( '( min-width:280px ) and ( max-width:767px )' ).matches) {
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
    empty_btn.classList.add('hidden')
    trade_item_info.classList.add('d-flex')
    back_btn.classList.add('d-flex')
    chat_room_form.classList.add('chat_room_open');
    trade_item_info.classList.remove('hidden');
    back_btn.classList.remove('hidden');
    chat_room_form.classList.remove('hidden');
    chat_input_form_width();
    if(chat_screen.className.indexOf('welcome') > -1){
        close_chat_room_form.classList.add('hidden')
    }else{close_chat_room_form.classList.remove('hidden')}
}
// 채팅방 하단UI의 너비를 조절하는 함수
function chat_input_form_width() {
    if(chat_room_form.className.indexOf('hidden') == -1){
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
    back_btn.classList.remove('d-flex')
    back_btn.classList.add('hidden')
    trade_item_info.classList.add('hidden')
    chat_input.value = ''
    chat_input.placeholder = '매시지를 입력하세요'
    chat_room_form.classList.add('hidden')
    trade_success_form.classList.add('hidden');
    trade_success_btn.removeAttribute('disabled')
    trade_success_text.style.color = '#2f363d'
    chat_screen.removeAttribute('class')
    notification_trade_state.classList.add('hidden')
    chat_write_trade_comment.classList.add('hidden')
    chat_view_trade_comment.classList.add('hidden')
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
    if(chat_room_form.className.indexOf('hidden') == -1){chat_room_close()}
    chat_form.classList.add('chat_close')
    chat_input_form1.classList.add('chat_input_form_close')
    chat_form.classList.add('hidden');
    chat_start_button.classList.remove('hidden');
    chat_form.classList.remove('chat_open')
    chat_input_form1.classList.remove('chat_input_form_open')
    if (window.matchMedia( '( min-width:280px ) and ( max-width:414px )' ).matches) {
        const body_scroll = document.documentElement
        const body_scrollY = body_scroll.style.top
        body_scroll.style.position = ''
        body_scroll.style.top = ''
        window.scrollTo(0, parseInt(body_scrollY || '0')*-1)
    }
}
//채팅방 목록에서 각 채팅방의 수신 채팅 수를 알려주는 UI의 가로에 맞게 높이를 자동 조절하는 함수
function send_count_height() {
    const send_count = document.getElementsByClassName('send_count')
    if(send_count.length > 0){
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
    chat_screen.scrollTop = chat_screen.scrollHeight
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
function from_consumer_to_seller() {
    const seller_user_id = document.getElementById('seller_user_id')
    seller_user_id.innerText = `${chat_screen.classList[3].slice(9)}`
    const nav_user_nickname = document.getElementsByClassName('nav_user_nickname')
    input_trade_comment.placeholder = `${nav_user_nickname[0].innerText}님의 진심이 담긴 한마디가 ${seller_user_id
        .innerText}님에게 큰 도움이 될 수 있습니다. 또한 거래후기는 수정 삭제가 불가하므로 신중히 작성 부탁드립니다.`
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
        if(t == 0){t = 12}
        return get_date = `오전 ${t}:${tm.slice(14,16)}`
    }
}
// 메시지의 종류별로 각 채팅방목록의 최신메시지 내용을 파싱하는 함수
function message_parsing(msg) {
    if(msg.message_type == '0'){
        return get_message = msg.message
    }else{
        return get_message = '사진을 보냈습니다.'
    }
}
// 메시지의 종류별로 채팅말풍선의 내용을 파싱하는 함수
function conversation_parsing(msg) {
    if(msg.message_type == '0'){
        return msg.message;
    }else{
        const image_conversation_html = `
            <img src="${msg.message}"  class="convo_img" alt="">`
        return image_conversation_html;
    }
}
// 메시지가 이미지이며 높이가 다소 길 경우 최대 길이를 400px로 하고 나머지는 자르는 함수
function image_conversation_resize(msg) {
    if(msg.message_type == '1'){
        const speech_bubble = document.getElementsByClassName('speech_bubble')
        speech_bubble[speech_bubble.length-1].style.padding = '0px'
        speech_bubble[speech_bubble.length-1].style.maxHeight = '400px'
        speech_bubble[speech_bubble.length-1].style.overflow = 'hidden'
        console.log('이미지 자르기 성공')
    }
}
// 메시지가 높이 400px이상의 이미지일 경우 포커싱을 가운데로 맞추는 함수
function image_conversation_focus() {
    const convo_img = document.getElementsByClassName('convo_img')
    if(convo_img.length > 0){
        for (let i = 0; i < convo_img.length; i++) {
            convo_img[i].onload = function(){
                console.log(convo_img[i].clientHeight)
                if(convo_img[i].clientHeight > 400){
                    convo_img[i].style.marginTop
                        = -(convo_img[i].clientHeight-400)/2+'px'
                    console.log('이미지 포커싱 성공')
                }
                chat_screen.scrollTop = chat_screen.scrollHeight
            }
        }
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
// 채팅방 목록의 html 코드를 제공하는 함수
function chat_list_js(res){
const chat_list_html =
    `<div class="card">
        <div class="card-body row d-flex flex-wrap align-content-center">
            <div class="item_image_form1 col-2 col-sm-2 d-flex flex-wrap align-content-center justify-content-end">
                <img src=${get_image}  class="rounded-circle item_image" alt="">
            </div>
            <div class="chat_list_right col-10 col-sm-10">
                <div class="clearfix chat_list_right_top">
                    <span class="chat_opponent_nickname float-left">
                        ${res.chatMessageUserDto.nickName}
                    </span>
                    <span class="room_no${res.roomId} chat_list_more_view float-right">
                        <i class="room_no${res.roomId} more_view_icon fas fa-ellipsis-v"></i>
                    </span>
                    <span class="send_time text-center float-right">
                        ${get_date}
                    </span>
                </div>
                <div class="d-flex align-items-center chat_list_right_bottom">
                    <div class="chat_conversation mr-auto">
                        ${get_message}
                    </div>
                    <span class="send_count d-flex justify-content-center 
                        align-items-center ml-auto rounded-circle"></span>
                </div>
            </div>
        </div>
    </div>
    <div class="room_no${res.roomId} more_view_form hidden">
        <div class="chat_notification_message notification_no${res.roomId}"></div>
        <div class="opponent_block user_no${res.chatMessageUserDto.userId}"></div>
        <div class="opponent_report" data-toggle="modal"
            data-target="#report_no${res.roomId}">신고</div>
        <div class="chat_room_leave" data-toggle="modal" 
            data-target="#chat_list_leave_chat_no${res.roomId}">나가기</div>
        <div class="more_view_cancel">취소</div>
    </div>
    <!-- 채팅 나가기 버튼 클릭 시 팝업창 생성 -->
    <div id="chat_list_leave_chat_no${res.roomId}" class="modal fade chat_room_out_modal">
        <div class="modal-dialog modal-sm">
            <div class="modal-content">
                <div class="modal-header">
                    <h6 class="modal-title">채팅방에서 나가시겠습니까?</h6>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body text-center">
                    <p>나가기를 하면 채팅방과 채팅 <br>내용이 모두 삭제됩니다.</p>
                    <button type="button" id="chat_list_leave_chat_btn_no${res.roomId}" 
                        class="btn btn-outline-danger chat_list_chat_room_out" data-dismiss="modal">나가기</button>
                    <button type="button" class="btn btn-outline-warning" data-dismiss="modal">취소</button>
                </div>
            </div>
        </div>
    </div>
    <!-- 신고하기 버튼 클릭 시 팝업창 view -->
    <div id="report_no${res.roomId}" class="modal fade chat_report_modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h6 class="modal-title">신고하기</h6>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body text-center">
                    <label for="report_category_no${res.roomId}"></label>
                    <select id="report_category_no${res.roomId}" class="custom-select">
                        <option value="">선택</option>
                        <option value="1">욕설 및 비방 등의 언어폭력</option>
                        <option value="2">성희롱</option>
                        <option value="3">전문판매업자 의심</option>
                        <option value="4">광고 및 홍보성 글</option>
                        <option value="5">사기피해 및 사기의심</option>
                        <option value="6">판매 금지 품목 업로드</option>
                        <option value="7">기타</option>
                    </select>
                    <div class="form-group">
                        <label for="input_report"></label>
                        <textarea class="form-control" rows="4" id="input_report_no${res.roomId}" placeholder="구체적인 사유를 적어주세요."></textarea>
                    </div>
                    <button type="button" class="btn btn-outline-success report_submit" data-dismiss="modal" id="report_submit_no${res.roomId}">전송</button>
                    <button type="button" class="btn btn-outline-warning" data-dismiss="modal">취소</button>
                </div>
            </div>
        </div>
    </div>`
    return chat_list_html;
}
// 읽지않은 전체 채팅갯수를 플로팅 버튼 위에 실시간으로 렌더링하는 함수
function unread_total_chat() {
    if( Number(total_alm.innerText) == 0){
        total_alm.innerText = Number(total_alm.innerText) + 1;
        total_alm.classList.remove('hidden')
    }else{
        if(Number(total_alm.innerText) <= 99){
            total_alm.innerText = Number(total_alm.innerText) + 1;
        }else{
            total_alm.innerText = '99+'
        }
    }
}
// 채팅방에 입장 후 읽지 않은 채팅 메시지를 읽음으로 바꿔주는 함수
function read_message(w_res) {
    if(w_res.who == chat_screen.classList[2].slice(11)){
        for (let i = 0; i < document.getElementsByClassName('my_conversation').length; i++) {
            if(document.getElementsByClassName('my_conversation')[i].innerHTML == '읽지 않음'){
                document.getElementsByClassName('my_conversation')[i].innerHTML = '읽음';
            }
        }
    }
}
// 채팅방에서 해당 중고물품 정보를 렌더링해주는 함수
function trade_item_info_view(s_res) {
    const trade_item_title = document.getElementById('trade_item_title')
    const trade_item_price = document.getElementById('trade_item_price')
    const trade_item_picture = document.getElementById('trade_item_picture')
    if(s_res.data.transaction_status_id != '4'){
        trade_item_picture.src = `${s_res.data.product_img_path}`
        trade_item_title.innerText = `${s_res.data.product_title}`
        trade_item_price.innerText = `${s_res.data.product_price.slice(1)} 원`
        trade_item_picture.classList.add('move_onePick')
    }
    if(s_res.data.transaction_status_id == '0'){
        trade_state.innerText = '[판매중]'
        trade_state.style.color = 'rgb(29, 161, 242)';
        // 고유 href 추가
    }
    else if(s_res.data.transaction_status_id == '1'){
        trade_state.innerText = '[예약중]'
        trade_state.style.color = '#19ce60';
        // 고유 href 추가
    }
    else if(s_res.data.transaction_status_id == '2'){
        trade_state.innerText = '[판매완료]';
        trade_state.style.color = '#8b8b8b';
        // 고유 href 추가
    }
    else if(s_res.data.transaction_status_id == '3'){
        trade_state.innerText = '[신고처리중]'
        trade_state.style.color = 'rgb(247, 47, 51)';
        // 고유 href 추가
    }else{
        trade_state.innerText = '[삭제]';
        trade_state.style.color = '#8b8b8b';
        trade_item_picture.removeAttribute('href')
        trade_item_picture.classList.remove('move_onePick')
    }
}
// 채팅방 목록 렌더링 시 상대 유저 차단여부에 따라 차단버튼의 이름을 파싱해주는 함수
function user_block(blk,i) {
    const opponent_block = document.getElementsByClassName('opponent_block')
    if(blk == false){
        opponent_block[i].innerHTML = '차단'
    }else{
        opponent_block[i].innerHTML = '차단해제'
    }
}
//차단을 하거나 당했을 경우 채팅방의 css가 바뀌는 함수
function state_block_effect() {
    chat_input.setAttribute('disabled','')
    chat_input.style.opacity = '0.8'
    chat_submit_btn.setAttribute('disabled','')
    chat_submit_btn.style.background = '#adb5bd'
    send_file_btn.setAttribute('disabled','')
    input_send_file.setAttribute('disabled','')
    send_file_text.style.color = '#adb5bd'
    notification_trade_state.classList.remove('hidden')
    chat_write_trade_comment.classList.add('hidden')
    chat_view_trade_comment.classList.add('hidden')
}
// 차단을 했을 경우 채팅방UI를 파싱하는 함수
function state_block() {
    block_text.innerText = '차단해제'
    block_icon.className ='fas fa-unlock-alt chat_button_icon'
    chat_input.value = ''
    chat_input.placeholder = '차단하신 상대방과 채팅을 할 수 없습니다.'
    notification_trade_state_text.innerText =
        `차단하신 ${chat_screen.classList[3].slice(9)}님과 채팅을 할 수 없습니다.`
}
// 차단을 당했을 경우 채팅방UI를 파싱하는 함수
function blocked_from_user() {
    chat_input.placeholder = '상대방에게 차단되어 채팅을 할 수 없습니다.'
    notification_trade_state_text.innerText =
        `${chat_screen.classList[3].slice(9)}님에게 차단되어 채팅을 할 수 없습니다.`
}
// 차단을 해제했을 경우 채팅방UI를 파싱하는 함수
function state_unblock() {
    block_text.innerText = '차단'
    block_icon.className ='fas fa-user-lock chat_button_icon'
    unblocked_from_user()
}
// 차단해제받았을 경우 채팅방UI를 파싱하는 함수
function unblocked_from_user() {
    chat_input.removeAttribute('disabled')
    chat_input.removeAttribute('style')
    chat_input.placeholder = '메시지를 입력하세요'
    chat_submit_btn.removeAttribute('disabled')
    chat_submit_btn.removeAttribute('style')
    send_file_btn.removeAttribute('disabled')
    input_send_file.removeAttribute('disabled')
    send_file_text.style.color = '#2f363d'
}
// 상대방 유저와의 차단 여부에 따라 채팅방UI를 파싱하는 함수
function block_state(s_res) {
    if(s_res.data.block_status != '3'){
        state_block_effect();
    }
    if(s_res.data.block_status == '1'){
        state_block()
    }
    else if(s_res.data.block_status == '2'){
        block_text.innerText = '차단'
        block_icon.className ='fas fa-user-lock chat_button_icon'
        chat_input.value = ''
        blocked_from_user()
    }
    else if(s_res.data.block_status == '3'){
        state_unblock()
    }
}
// 채팅방에서 해당 중고물품의 거래상태가 판매완료일 경우에만 알림창 보여주는 함수
function trade_state_view(s_res) {
    if(s_res.productStatus == '3'){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.add('hidden')
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
    }
    else if(s_res.productStatus == '2' && Object.keys(s_res).length == 1){
        trade_success_form.classList.remove('hidden')
        notification_trade_state.classList.add('hidden')
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.removeAttribute('disabled')
        trade_success_text.style.color = '#2f363d'
    }
    else if(s_res.productStatus == '2' && s_res.transactionStatus == '1' && Object.keys(s_res).length == 3){
        trade_success_form.classList.remove('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>${s_res.reviewer_nickname}님이 아직 후기를 작성하지 않았습니다.`
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.setAttribute('disabled','')
        trade_success_text.style.color = '#adb5bd'
    }else if(s_res.productStatus == '2' && s_res.transactionStatus == '1' && s_res.isReviewUpload == '1'){
        trade_success_form.classList.remove('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>${s_res.reviewer_nickname}님이 후기를 작성했습니다.`
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.remove('hidden')
        trade_success_btn.setAttribute('disabled','')
        trade_success_text.style.color = '#adb5bd'
    }else if(s_res.productStatus == '1' && s_res.transactionStatus == '1' && Object.keys(s_res).length == 2){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerText = '판매가 완료되었습니다. 후기를 작성할 수 있습니다.'
        chat_write_trade_comment.classList.remove('hidden')
        chat_view_trade_comment.classList.add('hidden')
    }else if(s_res.productStatus == '1' && s_res.transactionStatus == '1' && s_res.isReviewUpload == '1'){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>나의 후기를 볼 수 있습니다.`
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.remove('hidden')
    }else if(s_res.productStatus == '0'){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.add('hidden')
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
    }
}
//거래후기 완료를 요청한 후 거래상태를 변경하는 함수
function trade_success_view() {
    trade_state.innerText = '[판매완료]';
    trade_state.style.color = '#8b8b8b';
    notification_trade_state.classList.remove('hidden')
    notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>${chat_screen.classList[3].slice(9)}님이 아직 후기를 작성하지 않았습니다.`
    trade_success_btn.setAttribute('disabled','')
    trade_success_text.style.color = '#adb5bd'
}
// 중고물품 거래후기를 서버로부터 응답받은 후 렌더링하는 함수
function trade_comment(s_res) {
    const writer_trade_comment = document.getElementById('writer_trade_comment')
    const trade_comment_date = document.getElementById('trade_comment_date')
    const trade_comment_content = document.getElementById('trade_comment_content')
    date_parsing(s_res.data.review_date)
    writer_trade_comment.innerText = `${s_res.data.review_nickname}`
    trade_comment_date.innerText = `${get_date}`
    trade_comment_content.innerText = `${s_res.data.review_comment}`
}
// 동적 생성된 엘리먼트 중에서 해당 클래스를 갖고 있을 경우 이벤트 바인딩하는 함수(중복)
function hasClass(elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}
//이미지 파일만 필터링하는 함수(중복)
function image_extension_filter(event) {
    const pathpoint = event.target.value.lastIndexOf('.');
    const filepoint = event.target.value.substring(pathpoint+1);
    const filetype = filepoint.toLowerCase();
    if (filetype !== 'jpg' && filetype !== 'gif' && filetype !== 'png' &&
        filetype !== 'jpeg') {
        alert('bmp를 제외한 이미지 파일만 업로드할 수 있습니다.');
        event.target.value = ""
        return false;
    }else{
        return true;
    }
}