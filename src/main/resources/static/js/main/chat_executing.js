// 테스트 코드
web_items_search.addEventListener('keyup', function () {
    permission_notification_message();
    request_unread_total_chat();
})
chat_start_button.addEventListener('click',function () {
    chat_form_view();
})
document.addEventListener('click',function (event) {
    // onePick.html에서 '채팅으로 거래하기' 버튼 클릭 시 이벤트 실행
    if (event.target.id == "chat_trade_button") {
        // 테스트 코드
        const json = {
            "chatMessageUserDto": {
                "userId": 1,
                "nickName": "asd0296"
            },
            "data": {
                "product_id": 10
            }
        }
        chat_screen.classList.add(`item_no${json.data.product_id}`)
        chat_screen.classList.add('welcome')
        chat_screen.classList.add(`opponent_no${json.chatMessageUserDto.userId}`)
        chat_screen.classList.add(`nickname_${json.chatMessageUserDto.nickName}`)

        chat_form_view();
        chat_room_view();
        notification_msg_form.classList.add('hidden')
        block_form.classList.add('hidden')
        report_form.classList.add('hidden')
        request_trade_item_info(chat_screen);
    }
    // 모바일 채팅방에서 상대방 프로필,닉네임,거래중인 물품 이미지를 클릭할 경우 채팅창이 닫히도록 하기(해당 페이지로 이동되었다는 것을 보여주기 위함)
    if (hasClass(event.target, 'opponent_profile_picture') ||
        hasClass(event.target, 'opponent_nickname') ||
        event.target.id == 'trade_item_picture') {
        if (window.matchMedia('( min-width:280px ) and ( max-width:767px )').matches) {
            chat_form_close();
            unsubscribe_chat_room();
        }
    }
    // 채팅방 목록에서 해당 채팅방 나가기
    if (hasClass(event.target, 'chat_list_chat_room_out')) {
        request_chat_room_out(event.target)
    }
    // 채탕방 목록에서 해당 채팅방의 유저를 신고하기
    if (hasClass(event.target, 'report_submit')) {
        if(event.target.previousSibling.previousSibling.previousSibling.previousSibling.value == ''
            || event.target.previousSibling.previousSibling.children[1].value == ''){
            alert('신고 유형과 구체적인 사유를 모두 기입해주시기 바랍니다.')
        }else{
            request_report(event.target.previousSibling.previousSibling.previousSibling.previousSibling,
                event.target.id.slice(16), event.target.previousSibling.previousSibling.children[1])
        }
    }
    // 채팅방 목록에서 더보기 창이 뜬 상태에서 다른 곳 클릭하면 더보기 창 닫히도록 하기
    const more_view_form = document.getElementsByClassName('more_view_form')
    if (more_view_form.length > 0){
        for (let i = 0; i < more_view_form.length; i++) {
            if (more_view_form[i].className.indexOf('hidden') == -1 &&
                event.target.className.indexOf(more_view_form[i].classList[0]) == -1) {
                more_view_form[i].classList.add('hidden')
            }
        }
    }
    // 채팅방에서 스크롤 버튼 클릭 시 채팅방 화면 최하단으로 이동
    if(event.target.id == "chat_scroll_btn_form" || event.target.id == "chat_scroll_btn_icon"){
        chat_screen.scrollTop = chat_screen.scrollHeight;
    }
    // 채팅방에서 이미지 메시지를 클릭할 경우 새로운 창으로 원본사이즈의 이미지 보여주기
    if(hasClass(event.target, 'convo_img')){
        window.open(event.target.src)
    }
})
// 채팅방 목록 클릭시 해당 채팅방 열기
$(document).on('click','.click_chat_room',function () {
    chat_room_view()
    // 물건번호, 룸번호, 유저번호, 유저닉네임
    chat_screen.classList.add(this.classList[0])
    chat_screen.classList.add(this.classList[1])
    chat_screen.classList.add(this.classList[2])
    chat_screen.classList.add(this.classList[3])
    if(Notification.permission == 'granted'){
        notification_msg_form.classList.remove('hidden')
        const chat_notification_message = document.getElementsByClassName('chat_notification_message')
        notification_msg_text.innerText = chat_notification_message[$('.click_chat_room').index(this)].innerText
        if(chat_notification_message[$('.click_chat_room').index(this)].innerText == '알림끄기'){
            notification_msg_icon.className = 'fas fa-bell-slash chat_button_icon'
        }else{
            notification_msg_icon.className = 'fas fa-bell chat_button_icon'
        }
    }
    request_trade_item_info(this)
    join_chat_room(this)
    // 해당 채팅방의 읽지 않은 메시지 갯수를 읽지 않은 전체 메시지 갯수에서 빼기
    console.log('전체 읽지 않은 갯수',Number(total_alm.innerText))
    const send_count = document.getElementsByClassName('send_count')
    console.log('읽은 갯수를 포함하는 태그',send_count[$('.click_chat_room').index(this)].innerText)
    total_alm.innerText =
        Number(total_alm.innerText) - Number(send_count[$('.click_chat_room').index(this)].innerText)
    console.log('결과',Number(total_alm.innerText))
    if(total_alm.innerText == '0'){total_alm.classList.add('hidden')}
    send_count[$('.click_chat_room').index(this)].innerText = '0'
    send_count[$('.click_chat_room').index(this)].classList.add('hidden')
    send_count[$('.click_chat_room').index(this)].classList.remove('d-flex')
})
// 채팅목록에서 ...을 클릭할 때 채팅방으로 이동되지 않도록 이벤트 버블링 차단 후 더보기 팝엽창 열기
$(document).on('click','.chat_list_more_view',function (event) {
    event.stopPropagation()
    this.parentNode.parentNode.parentNode.parentNode
        .nextSibling.nextSibling.classList.toggle('hidden')
})
// 채팅목록에서 더보기 팝업창의 선택지를 클릭할 때 채팅방으로 이동되지 않도록 이벤트 버블링 차단 하기
$(document).on('click','.more_view_form',function (event) {
    event.stopPropagation()
})
$(document).on('click','.chat_notification_message',function () {
    if(Notification.permission == 'granted'){
        if (this.innerText == '알림끄기'){
            this.innerText = '알림켜기';
        }else{
            this.innerText = '알림끄기';
        }
        switch_notification_message(this.classList[1].slice(15))
    }
})
$(document).on('click','.opponent_block',function () {
    if(this.innerText == '차단'){
        request_user_block(this.classList[1].slice(7))
    }else{
        request_user_unblock(this.classList[1].slice(7));
    }
})
$(document).on('click','.more_view_cancel',function () {
    this.parentNode.classList.add('hidden')
})
// 채팅목록에서 채팅방 신고 모달UI의 버튼을 클릭할 때 채팅방으로 이동되지 않도록 이벤트 버블링 차단하기
$(document).on('click','.chat_report_modal',function (event) {
    event.stopPropagation()
})
// 채팅목록에서 채팅방 나가기 모달UI의 버튼을 클릭할 때 채팅방으로 이동되지 않도록 이벤트 버블링 차단하기
$(document).on('click','.chat_room_out_modal',function (event) {
    event.stopPropagation()
})
window.addEventListener('scroll',() => {
    document.documentElement.style.setProperty('--scroll-y',`${window.scrollY}px`)
})
chat_opponent_search.addEventListener('keyup', chat_room_search)
//채팅방 리스트 검색 입력란 안의 text를 일괄 삭제하는 함수
chat_search_icon.addEventListener('click',function () {
    chat_opponent_search.value = ''
    chat_room_search();
})
back_btn.addEventListener('click', function () {
    if(chat_screen.className.indexOf('chat_list_no') > -1){unsubscribe_chat_room();}
    chat_room_close();
})
close_btn.addEventListener('click',function () {
    if(chat_list_form.className == 'hidden' && chat_screen.className.indexOf('chat_list_no') > -1){unsubscribe_chat_room();}
    chat_form_close();
})
confirm_trade_success.addEventListener('click',request_trade_success)
chat_write_trade_comment.addEventListener('click',from_consumer_to_seller)
comment_submit.addEventListener('click', function (event) {
    if(input_trade_comment.value == ''){alert('구매후기를 작성해주세요')}
    else{
        request_upload_trade_comment(input_trade_comment.value,chat_screen.classList[0].slice(7),event)
    }
})
chat_view_trade_comment.addEventListener('click',function (event) {
    request_view_trade_comment(chat_screen.classList[0].slice(7), event)
})
document.addEventListener('keydown',function (event) {
    if (event.keyCode == 27){
        if(chat_list_form.className == 'hidden' && chat_screen.className.indexOf('chat_list_no') > -1){unsubscribe_chat_room();}
        chat_form_close();
    }
})
chat_room_out.addEventListener('click', function () {
    request_chat_room_out();
})
// 채팅 입력란의 내용이 공백뿐일 경우 채팅 전송 방지
chat_input.addEventListener( 'keydown' ,function(event) {
    if (event.keyCode == 13 && !event.shiftKey){
        // 엔터 클릭 후 개행 방지
        event.preventDefault();
        if (0 < chat_input.value.length && chat_input.value.replace(/^\s+|\s+$/g,"") != "") {
            send_message(chat_input.value,0)
        }
    }
})
chat_input.addEventListener('keyup',limit_string)
// 채팅 입력란의 내용이 공백뿐일 경우 채팅 전송 방지
chat_submit_btn.addEventListener('click',function() {
    if (0 < chat_input.value.length && chat_input.value.replace(/^\s+|\s+$/g,"") != "") {
        send_message(chat_input.value,0)
    }
})
notification_msg_btn.addEventListener('click',function () {
    if(Notification.permission == 'granted'){
        const chat_notification_message = document.getElementsByClassName('chat_notification_message')
        if(notification_msg_text.innerText == '알림끄기'){
            notification_msg_text.innerText = '알림켜기'
            notification_msg_icon.className = 'fas fa-bell chat_button_icon'
            for (let i = 0; i < chat_notification_message.length; i++) {
                if(chat_notification_message[i].classList[1].slice(15) == chat_screen.classList[1].slice(12)){
                    chat_notification_message[i].innerText = '알림켜기'
                }
            }
        }else{
            notification_msg_text.innerText = '알림끄기'
            notification_msg_icon.className = 'fas fa-bell-slash chat_button_icon'
            for (let i = 0; i < chat_notification_message.length; i++) {
                if(chat_notification_message[i].classList[1].slice(15) == chat_screen.classList[1].slice(12)){
                    chat_notification_message[i].innerText = '알림끄기'
                }
            }
        }
        switch_notification_message(chat_screen.classList[1].slice(12))
    }
})
block_btn.addEventListener('click',function () {
    if(block_text.innerText == '차단'){
        request_user_block(chat_screen.classList[2].slice(11))
    }else{
        request_user_unblock(chat_screen.classList[2].slice(11))
    }
})
report_submit.addEventListener('click',function () {
    if(report_category.value == '' || input_report.value == ''){
        alert('신고 유형과 구체적인 사유를 모두 기입해주시기 바랍니다.')
    }else{
        request_report(report_category, chat_screen.classList[1].slice(12), input_report)
    }
})
window.addEventListener('resize',function () {
    chat_input_form_width();
    chat_list_opponent_nickname_width();
    send_count_height();
    chat_list_latest_message_width();
    chat_screen_and_room_height();
})
// 브라우저를 닫았을 때 해당 채팅방에 대한 구독을 취소하는 함수
window.addEventListener("beforeunload", function () {
    return sub_room_and_user.unsubscribe();
});
chat_screen.addEventListener('scroll',function () {
    create_chat_scroll_btn()
    if(chat_screen.clientHeight + chat_screen.scrollTop >= chat_screen.scrollHeight){
        delete_chat_scroll_btn()
    }
})
