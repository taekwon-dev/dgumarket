// 채팅방 리스트 동적으로 생성하는 함수
function create_chat_list(s_res){
    for (let i = 0; i < s_res.length; i++) {
        const chat_div = document.createElement('div')
        chat_div.setAttribute('class',`item_no${s_res[i].chatRoomProductDto.product_id} 
            chat_list_no${s_res[i].roomId} opponent_no${s_res[i].chatMessageUserDto.userId} nickname_${s_res[i].chatMessageUserDto.nickName} 
            chat_list click_chat_room`);
        date_parsing(s_res[i].chatRoomRecentMessageDto.message_date)
        message_parsing(s_res[i].chatRoomRecentMessageDto)
        image_parsing(s_res[i].chatRoomProductDto)
        chat_div.innerHTML =
            `<div class="card">
                <div class="card-body row d-flex flex-wrap align-content-center">
                    <div class="item_image_form1 col-2 col-sm-2 d-flex flex-wrap align-content-center justify-content-end">
                        <img src=${get_image}  class="rounded-circle item_image" alt="">
                    </div>
                    <div class="chat_list_right col-10 col-sm-10">
                        <div class="clearfix chat_list_right_top">
                            <span class="chat_opponent_nickname float-left">
                                ${s_res[i].chatMessageUserDto.nickName}
                            </span>
                            <span class="room_no${s_res[i].roomId} chat_list_more_view float-right">
                                <i class="room_no${s_res[i].roomId} more_view_icon fas fa-ellipsis-v"></i>
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
            <div class="room_no${s_res[i].roomId} more_view_form hidden">
                <div class="chat_alarm">알람 끄기</div>
                <div class="opponent_block">차단</div>
                <div class="opponent_report">신고</div>
                <div class="chat_room_leave" data-toggle="modal" 
                    data-target="#chat_list_leave_chat_no${s_res[i].roomId}">나가기</div>
                <div class="more_view_cancel">취소</div>
            </div>
            <!-- 채팅 나가기 버튼 클릭 시 팝업창 생성 -->
            <div id="chat_list_leave_chat_no${s_res[i].roomId}" class="modal fade chat_room_out_modal">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h6 class="modal-title">채팅방에서 나가시겠습니까?</h6>
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                        </div>
                        <div class="modal-body text-center">
                            <p>나가기를 하면 채팅방과 채팅 <br>내용이 모두 삭제됩니다.</p>
                            <button type="button" id="chat_list_leave_chat_btn_no${s_res[i].roomId}" 
                                class="btn btn-outline-danger chat_list_chat_room_out" data-dismiss="modal">나가기</button>
                            <button type="button" class="btn btn-outline-warning" data-dismiss="modal">취소</button>
                        </div>
                    </div>
                </div>
            </div>`
        chat_list_space.appendChild(chat_div)
        //각 채팅방에서 미수신 메시지가 0인 경우 미수신 알람 미처리
        const send_count = document.getElementsByClassName('send_count')
        send_count[i].innerText = `${s_res[i].unreadMessageCount}`
        if(s_res[i].unreadMessageCount == '0'){
            send_count[i].classList.add('hidden');
            send_count[i].classList.remove('d-flex');
        }else if(Number(s_res[i].unreadMessageCount) > 99){
            send_count[i].innerText = '99+'
        }
    }
    has_chat_list();
    chat_list_opponent_nickname_width();
    chat_list_latest_message_width();
    send_count_height();
}
// 상대방으로부터 메시지가 올 때 해당 채팅목록에 최근 메시지 정보 및 갯수를 렌더링하는 함수
function latest_message(w_res) {
    let toggle = false;
    date_parsing(w_res.messageDate)
    message_parsing(w_res)
    image_parsing(w_res.chatRoomProductDto)
    for (let i = 0; i < document.getElementsByClassName('chat_list').length; i++) {
        if(document.getElementsByClassName('chat_list')[i].classList[1].indexOf(w_res.roomId) > -1){
            document.getElementsByClassName('send_time')[i].innerHTML = `${get_date}`
            document.getElementsByClassName('chat_conversation')[i].innerHTML = `${get_message}`
            document.getElementsByClassName('item_image')[i].innerHTML = `${get_image}`
            document.getElementsByClassName('chat_opponent_nickname')[i].innerHTML = `${w_res.chatMessageUserDto.nickName}`
            if( document.getElementsByClassName('send_count')[i].className.indexOf('hidden') > -1){
                document.getElementsByClassName('send_count')[i].classList.remove('hidden');
                document.getElementsByClassName('send_count')[i].classList.add('d-flex');
                document.getElementsByClassName('send_count')[i].innerHTML = '1'
            }else{
                if(Number(document.getElementsByClassName('send_count')[i].innerHTML) <= 99){
                    document.getElementsByClassName('send_count')[i].innerHTML =
                        Number(document.getElementsByClassName('send_count')[i].innerHTML) + 1;
                }else{
                    document.getElementsByClassName('send_count')[i].innerHTML = '99+'
                }
            }
            chat_list_space.insertBefore(document.getElementsByClassName('chat_list')[i], chat_list_space.firstChild)
            return toggle = true;
        }
    }
    if(toggle == false){
        const chat_div = document.createElement('div')
        chat_div.setAttribute('class',`item_no${w_res.chatRoomProductDto.product_id} 
            chat_list_no${w_res.roomId} opponent_no${w_res.chatMessageUserDto.userId} nickname_${w_res.chatMessageUserDto.nickName} 
            chat_list click_chat_room`);
        chat_div.innerHTML =
            `<div class="card">
                <div class="card-body row d-flex flex-wrap align-content-center">
                    <div class="item_image_form1 col-2 col-sm-2 d-flex flex-wrap align-content-center justify-content-end">
                        <img src=${get_image}  class="rounded-circle item_image" alt="">
                    </div>
                    <div class="chat_list_right col-10 col-sm-10">
                        <div class="clearfix chat_list_right_top">
                            <span class="chat_opponent_nickname float-left">
                                ${w_res.chatMessageUserDto.nickName}
                            </span>
                            <span class="room_no${w_res.roomId} chat_list_more_view float-right">
                                <i class="room_no${w_res.roomId} more_view_icon fas fa-ellipsis-v"></i>
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
            <div class="room_no${w_res.roomId} more_view_form hidden">
                <div class="chat_alarm">알람 끄기</div>
                <div class="opponent_block">차단</div>
                <div class="opponent_report">신고</div>
                <div class="chat_room_leave" data-toggle="modal" 
                    data-target="#chat_list_leave_chat_no${w_res.roomId}">나가기</div>
                <div class="more_view_cancel">취소</div>
            </div>
            <!-- 채팅 나가기 버튼 클릭 시 팝업창 생성 -->
            <div id="chat_list_leave_chat_no${w_res.roomId}" class="modal fade chat_room_out_modal">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h6 class="modal-title">채팅방에서 나가시겠습니까?</h6>
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                        </div>
                        <div class="modal-body text-center">
                            <p>나가기를 하면 채팅방과 채팅 <br>내용이 모두 삭제됩니다.</p>
                            <button type="button" id="chat_list_leave_chat_btn_no${w_res.roomId}" 
                                class="btn btn-outline-danger chat_list_chat_room_out" data-dismiss="modal">나가기</button>
                            <button type="button" class="btn btn-outline-warning" data-dismiss="modal">취소</button>
                        </div>
                    </div>
                </div>
            </div>`
        chat_list_space.insertBefore(chat_div, chat_list_space.firstChild)
        const send_count = document.getElementsByClassName('send_count')
        send_count[0].innerText = '1'
    }
}
// 읽지않은 전체 채팅갯수를 플로팅 버튼 위에 실시간 알람으로 렌더링하는 함수
function unread_total_chat() {
    if( total_alm.className.indexOf('hidden') > -1){
        total_alm.classList.remove('hidden')
        total_alm.innerText = '1'
    }else{
        if(Number(total_alm.innerText) <= 99){
            total_alm.innerText = Number(total_alm.innerText) + 1;
        }else{
            total_alm.innerText = '99+'
        }
    }
}
//채팅 입력란에 채팅 메시지를 입력 및 전송 후 채팅 말풍선을 동적 생성하여 렌더링하는 함수
function create_new_conversation(w_res) {
    time_parsing(w_res.messageDate)
    if(w_res.messageStatus == '0'){
        message_state = '읽지 않음'
    }else{message_state = '읽음'}
    //채팅방의 대화내용들을 연월일 기준으로 채팅 말풍선을 구분하는 UI를 생성하는 곳
    if(!document.querySelector('.latest_date')){
        const date_div = document.createElement('div')
        date_div.setAttribute('class','d-flex justify-content-center')
        date_div.innerHTML =
            `<span class="badge badge-primary m-3 latest_date">${w_res.messageDate.slice(0,4)}년 ${w_res.messageDate
                .slice(5,7)}월 ${w_res.messageDate.slice(8,10)}일 ${get_input_day_label(w_res.messageDate
                .slice(0,4),Number(w_res.messageDate.slice(5,7))-1,w_res.messageDate.slice(8,10))}</span>`
        chat_screen.appendChild(date_div)
    }else{
        if (document.getElementsByClassName('latest_date')[document.getElementsByClassName('latest_date').length-1]
            .innerText != `${w_res.messageDate.slice(0,4)}년 ${w_res.messageDate.slice(5,7)}월 ${w_res.messageDate
            .slice(8,10)}일 ${get_input_day_label(w_res.messageDate.slice(0,4),Number(w_res.messageDate
            .slice(5,7))-1,w_res.messageDate.slice(8,10))}`){
            const date_div = document.createElement('div')
            date_div.setAttribute('class','d-flex justify-content-center')
            date_div.innerHTML =
                `<span class="badge badge-primary m-3 latest_date">${w_res.messageDate.slice(0,4)}년 ${w_res.messageDate
                    .slice(5,7)}월 ${w_res.messageDate.slice(8,10)}일 ${get_input_day_label(w_res.messageDate
                    .slice(0,4),Number(w_res.messageDate.slice(5,7))-1,w_res.messageDate.slice(8,10))}</span>`
            chat_screen.appendChild(date_div)
        }
    }
    // 1분미만 연속으로 전송된 말풍선 동적생성하는 곳
    const conversation_form = document.getElementsByClassName('conversation_form')
    if(conversation_form.length > 0 && w_res.messageDate.slice(0,16) == conversation_form[conversation_form.length-1].classList[1]
        .slice(0,16) && w_res.chatMessageUserDto.userId == conversation_form[conversation_form.length-1].classList[2]){
        if (w_res.chatMessageUserDto.userId != web_items_search.value){
            const chat_bundle_div = document.createElement('div')
            chat_bundle_div.setAttribute('class',`conversation_form ${w_res.messageDate} 
                ${w_res.chatMessageUserDto.userId} d-flex`)
            chat_bundle_div.innerHTML =
                `<div class="opponent_speech_bubble">${w_res.message}</div>
                <div class="time_send_alm d-flex flex-wrap align-content-end">
                    <div class="receiving_time">${get_date}</div>
                </div>`
            const time_send_alm = document.getElementsByClassName('time_send_alm')
            conversation_form[conversation_form.length-1].removeChild(time_send_alm[time_send_alm.length-1])
            const opponent_form = document.getElementsByClassName('opponent_form')
            opponent_form[opponent_form.length-1].appendChild(chat_bundle_div)
        }else{
            const chat_bundle_div = document.createElement('div')
            chat_bundle_div.setAttribute('class','d-flex justify-content-end')
            chat_bundle_div.innerHTML=
                `<div class="time_send_alm d-flex flex-wrap align-content-end">
                    <div class="conversation_info">
                        <div class="my_conversation d-flex justify-content-end">${message_state}</div>
                        <div class="outgoing_time d-flex justify-content-end">${get_date}</div>
                    </div>
                </div>
                <div class="my_speech_bubble">${w_res.message}</div>`
            const conversation_info = document.getElementsByClassName('conversation_info')
            const outgoing_time = document.getElementsByClassName('outgoing_time')
            conversation_info[conversation_info.length-1].removeChild(outgoing_time[outgoing_time.length-1])
            conversation_form[conversation_form.length-1].appendChild(chat_bundle_div)
            chat_input.value = "";
        }
    }
    // 상대방 말풍선 동적생성하는 곳
    else if(w_res.chatMessageUserDto.userId != `${web_items_search.value}`){
        const chat_opponent_div = document.createElement('div')
        chat_opponent_div.setAttribute('class','opponent_form')
        chat_opponent_div.innerHTML =
            `<div class="d-flex flex-wrap align-content-center">
                <div>
                    <img src=${w_res.chatMessageUserDto.profileImgPath} href="/shop/item/myItem"
                         class="move_myItem rounded-circle opponent_profile_picture" alt="">
                </div>
                <div href="/shop/item/myItem" class="d-flex align-items-center move_myItem opponent_nickname">${w_res.chatMessageUserDto.nickName}</div>
            </div>
            <div class="conversation_form ${w_res.messageDate} ${w_res.chatMessageUserDto.userId} d-flex">
                <div class="opponent_speech_bubble">${w_res.message}</div>
                <div class="time_send_alm d-flex flex-wrap align-content-end">
                    <div class="receiving_time">${get_date}</div>
                </div>
            </div>`
        chat_screen.appendChild(chat_opponent_div)
    }
    // 본인 말풍선 동적생성하는 곳
    else{const chat_new_my_div = document.createElement('div');
        chat_new_my_div.setAttribute('class',`conversation_form ${w_res.messageDate} 
            ${w_res.chatMessageUserDto.userId} my_chat`);
        chat_new_my_div.innerHTML =
        `<div class="d-flex justify-content-end">
            <div class="time_send_alm d-flex flex-wrap align-content-end">
                <div class="conversation_info">
                    <div class="my_conversation d-flex justify-content-end">${message_state}</div>
                    <div class="outgoing_time d-flex justify-content-end">${get_date}</div>
                </div>
            </div>
            <div class="my_speech_bubble">${w_res.message}</div>
        </div>`
        chat_screen.appendChild(chat_new_my_div);
        chat_input.value = "";
    }
    chat_screen.scrollTop = chat_screen.scrollHeight
}
// 현재까지 상대방과 대화한 메시지를 동적생성하여 불러오는 함수
function create_conversation(w_res) {
    for (let i = 0; i < w_res.length; i++) {
        time_parsing(w_res[i].messageDate)
        if(w_res[i].messageStatus == '0'){
            message_state = '읽지 않음'
        }else{message_state = '읽음'}
        //채팅방의 대화내용들을 연월일 기준으로 채팅 말풍선을 구분하는 UI를 생성하는 곳
        if(!document.querySelector('.latest_date')){
            const date_div = document.createElement('div')
            date_div.setAttribute('class','d-flex justify-content-center')
            date_div.innerHTML =
                `<span class="badge badge-primary m-3 latest_date">${w_res[i].messageDate
                    .slice(0,4)}년 ${w_res[i].messageDate.slice(5,7)}월 ${w_res[i].messageDate
                    .slice(8,10)}일 ${get_input_day_label(w_res[i].messageDate.slice(0,4),Number(w_res[i].messageDate
                    .slice(5,7))-1,w_res[i].messageDate.slice(8,10))}</span>`
            chat_screen.appendChild(date_div)
        }else{
            if (document.getElementsByClassName('latest_date')[document.getElementsByClassName('latest_date').length-1]
                .innerText != `${w_res[i].messageDate.slice(0,4)}년 ${w_res[i].messageDate
                .slice(5,7)}월 ${w_res[i].messageDate.slice(8,10)}일 ${get_input_day_label(w_res[i].messageDate.slice(0,4),Number(w_res[i].messageDate
                .slice(5,7))-1,w_res[i].messageDate.slice(8,10))}`){
                const date_div = document.createElement('div')
                date_div.setAttribute('class','d-flex justify-content-center')
                date_div.innerHTML =
                    `<span class="badge badge-primary m-3 latest_date">${w_res[i].messageDate.slice(0,4)}년 ${w_res[i].messageDate
                        .slice(5,7)}월 ${w_res[i].messageDate.slice(8,10)}일 ${get_input_day_label(w_res[i].messageDate
                        .slice(0,4),Number(w_res[i].messageDate.slice(5,7))-1,w_res[i].messageDate.slice(8,10))}</span>`
                chat_screen.appendChild(date_div)
            }
        }
        // 1분미만 연속으로 전송된 말풍선 동적생성하는 곳
        const conversation_form = document.getElementsByClassName('conversation_form')
        if(conversation_form.length > 0 && w_res[i].messageDate.slice(0,16) == conversation_form[conversation_form.length-1].classList[1].slice(0,16) &&
            w_res[i].chatMessageUserDto.userId == conversation_form[conversation_form.length-1].classList[2]){
            if (w_res[i].chatMessageUserDto.userId != web_items_search.value){
                const chat_bundle_div = document.createElement('div')
                chat_bundle_div.setAttribute('class',`conversation_form ${w_res[i].messageDate} 
                ${w_res[i].chatMessageUserDto.userId} d-flex`)
                chat_bundle_div.innerHTML =
                    `<div class="opponent_speech_bubble">${w_res[i].message}</div>
                <div class="time_send_alm d-flex flex-wrap align-content-end">
                    <div class="receiving_time">${get_date}</div>
                </div>`
                const time_send_alm = document.getElementsByClassName('time_send_alm')
                conversation_form[conversation_form.length-1].removeChild(time_send_alm[time_send_alm.length-1])
                const opponent_form = document.getElementsByClassName('opponent_form')
                opponent_form[opponent_form.length-1].appendChild(chat_bundle_div)
            }else{
                const chat_bundle_div = document.createElement('div')
                chat_bundle_div.setAttribute('class','d-flex justify-content-end')
                chat_bundle_div.innerHTML=
                    `<div class="time_send_alm d-flex flex-wrap align-content-end">
                    <div class="conversation_info">
                        <div class="my_conversation d-flex justify-content-end">${message_state}</div>
                        <div class="outgoing_time d-flex justify-content-end">${get_date}</div>
                    </div>
                </div>
                <div class="my_speech_bubble">${w_res[i].message}</div>`
                const conversation_info = document.getElementsByClassName('conversation_info')
                const outgoing_time = document.getElementsByClassName('outgoing_time')
                conversation_info[conversation_info.length-1].removeChild(outgoing_time[outgoing_time.length-1])
                conversation_form[conversation_form.length-1].appendChild(chat_bundle_div)
                chat_input.value = "";
            }
        }
        // 상대방 말풍선 불러오는 곳
        else if(w_res[i].chatMessageUserDto.userId != `${web_items_search.value}`){
            const chat_opponent_div = document.createElement('div')
            chat_opponent_div.setAttribute('class','opponent_form')
            chat_opponent_div.innerHTML =
                `<div class="d-flex flex-wrap align-content-center">
                    <div>
                        <img src=${w_res[i].chatMessageUserDto.profileImgPath} href="/shop/item/myItem"
                             class="move_myItem rounded-circle opponent_profile_picture" alt="">
                    </div>
                    <div href="/shop/item/myItem" class="d-flex align-items-center move_myItem opponent_nickname">${w_res[i].chatMessageUserDto.nickName}</div>
                </div>
                <div class="conversation_form ${w_res[i].messageDate} ${w_res[i].chatMessageUserDto.userId} d-flex">
                    <div class="opponent_speech_bubble">${w_res[i].message}</div>
                    <div class="time_send_alm d-flex flex-wrap align-content-end">
                        <div class="receiving_time">${get_date}</div>
                    </div>
                </div>`
            chat_screen.appendChild(chat_opponent_div)
        }
        // 본인 말풍선 불러오는 곳
        else{const chat_my_div = document.createElement('div');
            chat_my_div.setAttribute('class',`conversation_form ${w_res[i].messageDate} 
                ${w_res[i].chatMessageUserDto.userId} my_chat`);
            chat_my_div.innerHTML =
                `<div class="d-flex justify-content-end">                
                    <div class="time_send_alm d-flex flex-wrap align-content-end">
                        <div class="conversation_info">
                            <div class="my_conversation d-flex justify-content-end">${message_state}</div>
                            <div class="outgoing_time d-flex justify-content-end">${get_date}</div>
                        </div>
                    </div>
                    <div class="my_speech_bubble">${w_res[i].message}</div>
                </div>`
            chat_screen.appendChild(chat_my_div);
            chat_input.value = "";
        }
        chat_screen.scrollTop = chat_screen.scrollHeight
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
    const trade_state = document.getElementById('trade_state')
    if(s_res.data.transaction_status_id != '4'){
        trade_item_picture.src = `${s_res.data.product_img_path}`
        trade_item_title.innerText = `${s_res.data.product_title}`
        trade_item_price.innerText = `${s_res.data.product_price.slice(1)} 원`
    }
    if(s_res.data.transaction_status_id == '0'){
        trade_state.innerText = '[판매중]'
        trade_state.style.color = 'rgb(29, 161, 242)';
        // 고유 href 추가
        trade_item_picture.classList.add('move_onePick')
    }
    else if(s_res.data.transaction_status_id == '1'){
        trade_state.innerText = '[예약중]'
        trade_state.style.color = '#19ce60';
        // 고유 href 추가
        trade_item_picture.classList.add('move_onePick')

    }
    else if(s_res.data.transaction_status_id == '2'){
        trade_state.innerText = '[판매완료]';
        trade_state.style.color = '#8b8b8b';
        // 고유 href 추가
        trade_item_picture.classList.add('move_onePick')

    }
    else if(s_res.data.transaction_status_id == '3'){
        trade_state.innerText = '[신고처리중]'
        trade_state.style.color = 'rgb(247, 47, 51)';
        // 고유 href 추가
        trade_item_picture.classList.add('move_onePick')

    }else{
        trade_state.innerText = '[삭제]';
        trade_state.style.color = '#8b8b8b';
        trade_item_picture.removeAttribute('href')
        trade_item_picture.classList.remove('move_onePick')

    }
}
// 채팅방에서 해당 중고물품의 거래상태가 판매완료일 경우에만 알림창 보여주는 함수
function trade_state(s_res) {
    if(s_res.productStatus == '3'){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.add('hidden')
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.removeAttribute('disabled')
    }
    else if(s_res.productStatus == '2' && Object.keys(s_res).length == 1){
        trade_success_form.classList.remove('hidden')
        notification_trade_state.classList.add('hidden')
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.removeAttribute('disabled')
    }
    else if(s_res.productStatus == '2' && s_res.transactionStatus == '1' && Object.keys(s_res).length == 3){
        trade_success_form.classList.remove('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>${s_res.reviewer_nickname}님이 아직 후기를 작성하지 않았습니다.`
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.setAttribute('disabled','')
    }else if(s_res.productStatus == '2' && s_res.transactionStatus == '1' && s_res.isReviewUpload == '1'){
        trade_success_form.classList.remove('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>${s_res.reviewer_nickname}님이 후기를 작성했습니다.`
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.remove('hidden')
        trade_success_btn.setAttribute('disabled','')
    }else if(s_res.productStatus == '1' && s_res.transactionStatus == '1' && Object.keys(s_res).length == 2){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerText = '판매가 완료되었습니다. 후기를 작성할 수 있습니다.'
        chat_write_trade_comment.classList.remove('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.removeAttribute('disabled')
    }else if(s_res.productStatus == '1' && s_res.transactionStatus == '1' && s_res.isReviewUpload == '1'){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.remove('hidden')
        notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>나의 후기를 볼 수 있습니다.`
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.remove('hidden')
        trade_success_btn.removeAttribute('disabled')
    }else if(s_res.productStatus == '0'){
        trade_success_form.classList.add('hidden')
        notification_trade_state.classList.add('hidden')
        chat_write_trade_comment.classList.add('hidden')
        chat_view_trade_comment.classList.add('hidden')
        trade_success_btn.removeAttribute('disabled')
    }
}
//거래후기 완료를 요청한 후 거래상태를 변경하는 함수
function trade_success_view() {
    const trade_state = document.getElementById('trade_state')
    trade_state.innerText = '[판매완료]';
    trade_state.style.color = '#8b8b8b';
    notification_trade_state.classList.remove('hidden')
    notification_trade_state_text.innerHTML = `판매가 완료되었습니다.<br>${chat_screen.classList[3].slice(9)}님이 아직 후기를 작성하지 않았습니다.`
    trade_success_btn.setAttribute('disabled','')
}
// 중고물품 거래후기 정보를 서버로부터 응답받은 후 렌더링하는 함수
function trade_comment(s_res) {
    const writer_trade_comment = document.getElementById('writer_trade_comment')
    const trade_comment_date = document.getElementById('trade_comment_date')
    const trade_comment_content = document.getElementById('trade_comment_content')
    date_parsing(s_res.data.review_date)
    writer_trade_comment.innerText = `${s_res.data.review_nickname}`
    trade_comment_date.innerText = `${get_date}`
    trade_comment_content.innerText = `${s_res.data.review_comment}`
}
// 채팅방의 스크롤을 움직일 경우 스크롤을 채팅방의 최하단으로 이동시키는 버튼 생성하는 함수
function create_chat_scroll_btn() {
    const chat_scroll_btn_form = document.getElementById('chat_scroll_btn_form')
    if(!chat_scroll_btn_form){
        const scroll_div = document.createElement('div')
        scroll_div.setAttribute('id','chat_scroll_btn_form')
        scroll_div.setAttribute('class','d-flex align-items-center justify-content-center rounded-circle')
        scroll_div.innerHTML = `<i id="chat_scroll_btn_icon" class="far fa-hand-point-down"></i>`
        chat_screen.appendChild(scroll_div)
    }
}
// 채팅방의 스크롤을 채팅방의 최하단으로 이동시키는 버튼 지우는 함수
function delete_chat_scroll_btn() {
    const chat_scroll_btn_form = document.getElementById('chat_scroll_btn_form')
    chat_screen.removeChild(chat_scroll_btn_form)
}