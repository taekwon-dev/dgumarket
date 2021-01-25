//테스트 코드
$(document).ready(function(){
    request_unread_total_chat()
});
// 테스트 코드
web_items_search.addEventListener('keyup',websocket_connect)

chat_start_button.addEventListener('click',request_my_chat_list)
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
        //product_id를 class에 저장하기
        chat_screen.classList.add(`item_no${json.data.product_id}`)
        //roomId를 class에 저장하기
        chat_screen.classList.add(`welcome`)
        //상대방 user_id를 class에 저장하기
        chat_screen.classList.add(`opponent_no${json.chatMessageUserDto.userId}`)
        //상대방 nickname을 class에 저장하기
        chat_screen.classList.add(`nickname_${json.chatMessageUserDto.nickName}`)

        request_trade_item_info(chat_screen)
        chat_form_view()
        chat_room_view()
    }
    // 모바일 채팅방에서 상대방 프로필,닉네임,거래중인 물품 이미지를 클릭할 경우 채팅창이 닫히도록 하기(해당 페이지로 이동되었다는 것을 보여주기 위함)
    if (hasClass(event.target, 'opponent_profile_picture') ||
        hasClass(event.target, 'opponent_nickname') ||
        event.target.id == 'trade_item_picture') {
        if (window.matchMedia('( min-width:280px ) and ( max-width:767px )').matches) {
            chat_form_close();
            request_unread_total_chat();
            unsubscribe_chat_room();
        }
    }
    // 채팅방 목록에서 해당 채팅방 삭제하기
    if (hasClass(event.target, 'chat_list_chat_room_out')) {
        chat_list_space.removeChild(event.target.parentNode.parentNode.parentNode.parentNode.parentNode)
        has_chat_list()
        request_chat_room_out(event.target.id)
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
    if(event.target.id == "chat_scroll_btn_form" || event.target.id == "chat_scroll_btn_icon"){
        chat_screen.scrollTop = chat_screen.scrollHeight;
    }
})
// 채팅방 목록 클릭시 해당 채팅방 열기
$(document).on('click','.click_chat_room',function () {
    chat_room_view()
    request_trade_item_info(this)
    request_trade_state(this)
    join_chat_room(this)
    //product_id를 class에 저장하기
    chat_screen.classList.add(this.classList[0])
    //roomId를 class에 저장하기
    chat_screen.classList.add(this.classList[1])
    //상대방 user_id를 class에 저장하기
    chat_screen.classList.add(this.classList[2])
    //상대방 nickname을 class에 저장하기
    chat_screen.classList.add(this.classList[3])
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
// 채팅목록에서 취소를 클릭할 때 채팅방으로 이동되지 않도록 이벤트 버블링 차단하기
$(document).on('click','.more_view_cancel',function () {
    this.parentNode.classList.add('hidden')
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
    request_my_chat_list()
})
close_btn.addEventListener('click',function () {
    if(chat_list_form.className == 'hidden' && chat_screen.className.indexOf('chat_list_no') > -1){unsubscribe_chat_room();}
    chat_form_close();request_unread_total_chat();
})
confirm_trade_success.addEventListener('click',request_trade_success)
chat_write_trade_comment.addEventListener('click',from_seller_to_consumer)
comment_submit.addEventListener('click',request_upload_trade_comment)
chat_view_trade_comment.addEventListener('click',request_view_trade_comment)
document.addEventListener('keydown',function (event) {
    if (event.keyCode == 27){
        if(chat_list_form.className == 'hidden' && chat_screen.className.indexOf('chat_list_no') > -1){unsubscribe_chat_room();}
        chat_form_close();
        request_unread_total_chat();
    }
})
chat_room_out.addEventListener('click', function () {
    request_chat_room_out();
    unsubscribe_chat_room();
    request_my_chat_list();
})
// 채팅 입력란의 내용이 공백뿐일 경우 채팅 전송 방지
chat_input.addEventListener( 'keydown' ,function(event) {
    if (event.keyCode == 13 && !event.shiftKey){
        // 엔터 클릭 후 개행 방지
        event.preventDefault();
        if (0 < chat_input.value.length && chat_input.value.replace(/^\s+|\s+$/g,"") !== "") {
            send_message()
        }
    }
})
chat_input.addEventListener('keyup',limit_string)
// 채팅 입력란의 내용이 공백뿐일 경우 채팅 전송 방지
chat_submit_btn.addEventListener('click',function() {
    if (0 < chat_input.value.length && chat_input.value.replace(/^\s+|\s+$/g,"") !== "") {
        send_message()
    }
})
window.addEventListener('resize',function () {
    chat_input_form_width();chat_list_opponent_nickname_width();send_count_height();
    chat_list_latest_message_width();chat_screen_and_room_height();
})
// 브라우저를 닫았을 때 해당 채팅방에 대한 구독을 취소하는 함수
window.addEventListener("beforeunload", function () {
    return sub_room_and_user.unsubscribe();
});
chat_screen.addEventListener('scroll',function () {
    create_chat_scroll_btn()
    console.log(chat_screen.scrollHeight,chat_screen.scrollTop)
    if(chat_screen.clientHeight + chat_screen.scrollTop >= chat_screen.scrollHeight){
        delete_chat_scroll_btn()
    }
})
