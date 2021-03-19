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
        chat_div.innerHTML = chat_list_js(s_res[i])
        chat_list_space.appendChild(chat_div)
        user_block(s_res[i].block,i)
        if(Notification.permission == 'granted'){chat_list_notification_message(s_res[i],i)}
        //각 채팅방에서 미수신 메시지가 0인 경우 미수신 알림 미처리
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
// 채팅 거래를 통해 새로운 채팅방 목록을 생성하는 함수
function new_create_chat_list(w_res) {
    // 날짜, 메시지, 이미지 파싱
    date_parsing(w_res.messageDate)
    message_parsing(w_res)
    get_image = document.getElementById('trade_item_picture').src
    chat_screen.className =
        `${chat_screen.classList[0]} chat_list_no${room_id} ${chat_screen.classList[2]} ${chat_screen.classList[3]}`
    close_chat_room_form.classList.remove('hidden')
    // 채팅방 생성
    const chat_div = document.createElement('div')
    chat_div.setAttribute('class',`${chat_screen.classList[0]} 
        ${chat_screen.classList[1]} ${chat_screen.classList[2]} 
        ${chat_screen.classList[3]} chat_list click_chat_room`);
    chat_div.innerHTML = chat_list_js(w_res)
    chat_list_space.insertBefore(chat_div, chat_list_space.firstChild)
    // 채팅방목록의 닉네임, 미수신 개수, 더보기창의 차단UI 파싱
    const chat_opponent_nickname = document.querySelector('.chat_opponent_nickname')
    chat_opponent_nickname.innerText = `${chat_screen.classList[3].slice(9)}`
    const send_count = document.querySelector('.send_count')
    send_count.innerText = '0'
    send_count.classList.add('hidden')
    send_count.classList.remove('d-flex')
    const opponent_block = document.querySelector('.opponent_block')
    opponent_block.innerText = '차단'
    block_form.classList.remove('hidden')
    report_form.classList.remove('hidden')
    if(Notification.permission == 'granted'){
        const chat_notification_message = document.querySelector('.chat_notification_message')
        chat_notification_message.innerText = '알림끄기'
        notification_msg_form.classList.remove('hidden')
    }
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
            if(w_res.chatMessageUserDto.userId != `${web_items_search.value}`){
                if(chat_room_form.className.indexOf('hidden') > -1 || w_res.roomId != chat_screen.classList[1].slice(12)){
                    const send_count = document.getElementsByClassName('send_count')
                    if( send_count[i].innerText == '0'){
                        send_count[i].innerText = Number(send_count[i].innerText) + 1;
                        send_count[i].classList.remove('hidden');
                        send_count[i].classList.add('d-flex');
                        console.log('안 읽은 메시지가 1개다.')
                    }else{
                        if(Number(send_count[i].innerText) <= 99){
                            send_count[i].innerText =
                                Number(send_count[i].innerText) + 1;
                            console.log('안 읽은 메시지가 1개 추가되었다.')
                        }else{
                            send_count[i].innerText = '99+'
                            console.log('안 읽은 메시지가 99개 이상이다.')
                        }
                    }
                }
            }
            chat_list_space.insertBefore(document.getElementsByClassName('chat_list')[i], chat_list_space.firstChild)
            return toggle = true;
        }
    }
    // 새로운 거래 혹은 채팅방 나간 이후 해당 채팅방에 있는 상대방에게 메시지를 받아 채팅방이 생성되는 경우
    if(toggle == false){
        const chat_div = document.createElement('div')
        chat_div.setAttribute('class',`item_no${w_res.chatRoomProductDto.product_id} 
            chat_list_no${w_res.roomId} opponent_no${w_res.chatMessageUserDto.userId} nickname_${w_res.chatMessageUserDto.nickName} 
            chat_list click_chat_room`);
        chat_div.innerHTML = chat_list_js(w_res)
        chat_list_space.insertBefore(chat_div, chat_list_space.firstChild)
        const opponent_block = document.querySelector('.opponent_block')
        opponent_block.innerText = '차단'
        const send_count = document.querySelector('.send_count')
        send_count.innerText = '1'
        if(Notification.permission == 'granted'){
            const chat_notification_message = document.querySelector('.chat_notification_message')
            chat_notification_message.innerText = '알림끄기'
            localStorage_notification_message(w_res)
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
                `<div class="speech_bubble opponent_speech_bubble">${conversation_parsing(w_res)}</div>
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
                <div class="speech_bubble my_speech_bubble">${conversation_parsing(w_res)}</div>`
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
                <div class="speech_bubble opponent_speech_bubble">${conversation_parsing(w_res)}</div>
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
            <div class="speech_bubble my_speech_bubble">${conversation_parsing(w_res)}</div>
        </div>`
        chat_screen.appendChild(chat_new_my_div);
        chat_input.value = "";
    }
    image_conversation_resize(w_res)
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
                    `<div class="speech_bubble opponent_speech_bubble">${conversation_parsing(w_res[i])}</div>
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
                <div class="speech_bubble my_speech_bubble">${conversation_parsing(w_res[i])}</div>`
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
                    <div class="speech_bubble opponent_speech_bubble">${conversation_parsing(w_res[i])}</div>
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
                    <div class="speech_bubble my_speech_bubble">${conversation_parsing(w_res[i])}</div>
                </div>`
            chat_screen.appendChild(chat_my_div);
            chat_input.value = "";
        }
        image_conversation_resize(w_res[i])
    }
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