let stomp_client = null;
let sub_room = null;
let sub_room_and_user = null;
let room_id = null;

// 모든 채팅방 통틀어서 읽지 않은 메시지 갯수를 요청하는 함수
function request_unread_total_chat() {
    const reqPromise = fetch('/chat/user/unread/messages', {
        method: 'GET',
        headers : {Accept : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('읽지 않은 모든 메시지 갯수 요청 성공')
            return res.json();
        }else{
            console.log('읽지 않은 모든 메시지 갯수 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            if(data.data.unreadMessagesCnt == '0'){
                total_alm.innerText = `${data.data.unreadMessagesCnt}`
            }else{
                total_alm.innerText = `${data.data.unreadMessagesCnt}`
                total_alm.classList.remove('hidden')
                if(Number(data.data.unreadMessagesCnt) > 99){
                    total_alm.innerText = '99+'
                }
            }
            request_my_chat_list()
        })
        .catch(error => {
            console.log(error)
        })
}
// 사용자가 이용하고 있는 채팅방의 목록을 요청하는 함수
function request_my_chat_list() {
    const loading_chat_list_room = document.getElementById('loading_chat_list_room')
    loading_chat_list_room.classList.remove('hidden')
    const reqPromise = fetch('/chat/rooms', {
        method: 'GET',
        headers : {Accept : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('채팅방 목록 요청 성공')
            return res.json();
        }else{
            console.log('채팅방 목록 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            loading_chat_list_room.classList.add('hidden')
            if(data.length > 0){
                if(Notification.permission == 'granted'){localStorage_notification_message(data)}
                create_chat_list(data);
            }else{
                chat_room_empty.classList.remove('hidden')
            }
            websocket_connect();
        })
        .catch(error => {
            console.log(error)
        })
}
//채팅방의 해당 중고물품 데이터 요청하는 함수
function request_trade_item_info(click_chat_room) {
    const reqPromise = fetch(`/chat/room/section/product/${click_chat_room.classList[0].slice(7)}`, {
        method: 'GET',
        headers : {Accept : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('채팅방 해당 중고물품 데이터 요청 성공')
            return res.json();
        }else{
            console.log('채팅방 해당 중고물품 데이터 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            trade_item_info_view(data)
            if(chat_screen.className.indexOf('chat_list_no') > -1){request_block_state(click_chat_room)}
        })
        .catch(error => {
            console.log(error)
        })
}
// 채팅방 입장 시 채팅 상대와의 차단 여부 요청하는 함수
function request_block_state(click_chat_room) {
    const reqPromise = fetch(`/user/block/${click_chat_room.classList[2].slice(11)}`, {
        method: 'GET',
        headers : {Accept : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('차단상태 조회 요청 성공')
            return res.json();
        }else{
            console.log('차단상태 조회 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            block_state(data);
            chat_screen_and_room_height();
            if(data.data.block_status == '3'){request_trade_state(click_chat_room)}
        })
        .catch(error => {
            console.log(error)
        })
}
//채팅방에서 중고물품의 현재 거래완료 및 후기작성 유무 요청하는 함수
function request_trade_state(click_chat_room) {
    const reqPromise = fetch(`/chat/room/${click_chat_room.classList[1].slice(12)}/status`, {
        method: 'GET',
        headers : {Accept : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('중고물품 현재 거래상태 요청 성공')
            return res.json();
        }else{
            console.log('중고물품 현재 거래상태 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            trade_state_view(data);
            chat_screen_and_room_height();
        })
        .catch(error => {
            console.log(error)
        })
}
//채팅방에서 거래완료 요청하는 함수
function request_trade_success() {
    const param = {transaction_status_id : '2'}
    const reqPromise = fetch(`/chat/room/${chat_screen.classList[1].slice(12)}/product`, {
        method: 'PATCH',
        body: JSON.stringify(param),
        headers : {'Content-Type' : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('채팅방 해당 중고물품 거래완료 요청 성공')
            console.log(JSON.stringify(param))
            return res.text();
        }else{
            console.log('채팅방 해당 중고물품 거래완료 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            if(data == 'transaction_status_updated_fail'){
                console.log(data);
                alert('상대방을 차단한 상태에서 거래완료를 할 수 없습니다.')
            }else{
                console.log(data);
                trade_success_view();
                chat_screen_and_room_height();
            }
        })
        .catch(error => {
            console.log(error)
        })
}
// 유저에 대해 차단 요청하는 함수
function request_user_block(user_id) {
    const param = { block_user : user_id }
    const reqPromise = fetch('/user/block', {
        method: 'POST',
        body: JSON.stringify(param),
        headers : {'Content-Type' : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('유저 차단 요청 성공')
            console.log(JSON.stringify(param))
            return res.text();
        }else{
            console.log('유저 차단 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            if(data == 'block success'){
                const block = document.getElementsByClassName(`user_no${user_id}`)
                for (let i = 0; i < block.length; i++) {block[i].innerText = '차단해제'}
                if(chat_room_form.className.indexOf('hidden') == -1){
                    state_block_effect();
                    state_block();
                    trade_success_btn.setAttribute('disabled','')
                    trade_success_text.style.color = '#adb5bd'
                    chat_screen_and_room_height();
                }
                alert('차단이 정상적으로 완료되었습니다.')
            }else{
                alert('예전에 거래를 했던 유저의 경우 차단할 수 없습니다.')
            }
        })
        .catch(error => {
            console.log(error)
        })
}
// 유저에 대해 차단 해제 요청하는 함수
function request_user_unblock(user_id) {
    const reqPromise = fetch(`/user/block/${user_id}`, {
        method: 'DELETE',
        headers : {}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('유저 차단 해제 요청 성공')
            return res.text();
        }else{
            console.log('유저 차단 해제 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            const block = document.getElementsByClassName(`user_no${user_id}`)
            for (let i = 0; i < block.length; i++) {block[i].innerText = '차단'}
            if(chat_room_form.className.indexOf('hidden') == -1) {
                state_unblock();
                notification_trade_state.classList.add('hidden')
                request_trade_state(chat_screen);
                chat_screen_and_room_height();
            }
            alert('차단이 정상적으로 해제되었습니다.')
        })
        .catch(error => {
            console.log(error)
        })
}
//채팅방에서 해당 중고물품의 거래후기 작성 후 업로드를 요청하는 함수
function request_upload_trade_comment() {
    const param = {product_comment : input_trade_comment.value}
    const reqPromise = fetch(`/api/product/${chat_screen.classList[0].slice(7)}/comment`, {
        method: 'POST',
        body: JSON.stringify(param),
        headers : {'Content-Type' : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('채팅방 해당 중고물품 거래후기 업로드 요청 성공')
            console.log(JSON.stringify(param))
            return res.text();
        }else{
            console.log('채팅방 해당 중고물품 거래후기 업로드 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            chat_write_trade_comment.classList.add('hidden')
            chat_view_trade_comment.classList.remove('hidden')
        })
        .catch(error => {
            console.log(error)
        })
}
// 채팅방에서 해당 중고물품의 거래후기 조회 요청하는 함수
function request_view_trade_comment() {
    const reqPromise = fetch(`/api/product/${chat_screen.classList[0].slice(7)}/comment`, {
        method: 'GET',
        headers : {Accept : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('중고물품 거래후기 조회 요청 성공')
            return res.json();
        }else{
            console.log('중고물품 거래후기 조회 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            trade_comment(data)
        })
        .catch(error => {
            console.log(error)
        })
}
// 채팅방 나가기를 요청하는 함수
function request_chat_room_out(leave_btn) {
    let room_id;
    if(chat_list_form.className.indexOf('hidden') == -1){
        room_id = leave_btn.id.slice(27)
    }else{
        room_id = chat_screen.classList[1].slice(12)
    }
    const param = {room_leave : true}
    const reqPromise = fetch(`/chat/room/${room_id}`, {
        method: 'PATCH',
        body: JSON.stringify(param),
        headers : {'Content-Type' : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('채팅방 나가기 요청 성공')
            console.log(JSON.stringify(param))
            return res.text();
        }else{
            console.log('채팅방 나가기 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            if(chat_list_form.className.indexOf('hidden') == -1){
                const send_count = document.getElementsByClassName('send_count')
                total_alm.innerText =
                    Number(total_alm.innerText) - Number(send_count[$('.chat_list_chat_room_out').index(leave_btn)].innerText)
                if(total_alm.innerText == '0'){total_alm.classList.add('hidden');}
                const chat_list = document.getElementsByClassName('chat_list')
                chat_list_space.removeChild(chat_list[$('.chat_list_chat_room_out').index(leave_btn)])
            }else{
                unsubscribe_chat_room();
                chat_list_space.removeChild(document.getElementsByClassName(chat_screen.classList[1])[0])
                chat_room_close();
            }
            has_chat_list()
        })
        .catch(error => {
            console.log(error)
        })
}
// 유저 혹은 중고물품에 대해 신고 요청하는 함수
function request_report(cat, room, rsn) {
    const params = {
        report_category_id : cat.value,
        report_room_id : room,
        report_etc_reason : rsn.value
    }
    const reqPromise = fetch('/report', {
        method: 'POST',
        body: JSON.stringify(params),
        headers : {'Content-Type' : 'application/json'}
    })
    reqPromise.then(res => {
        if (res.status >= 200 && res.status < 300){
            console.log('유저 신고 요청 성공')
            console.log(JSON.stringify(params))
            return res.text();
        }else{
            console.log('유저 신고 요청 실패')
            return Promise.reject(new Error(res.status))
        }
    })
        .then(data => {
            console.log(data);
            alert('신고가 정상적으로 처리되었습니다.')
            cat.value = '';
            rsn.value = '';
        })
        .catch(error => {
            console.log(error)
        })
}
//웹소켓 연결 및 구독하는 함수
function websocket_connect() {
    let socket = new SockJS('/ws');
    stomp_client = Stomp.over(socket);
    stomp_client.connect({}, function() {
        stomp_client.subscribe(`/user/queue/room/event`, function (convo_frame) {
            if(JSON.parse(convo_frame.body).roomId){
                room_id = JSON.parse(convo_frame.body).roomId
                sub_room = stomp_client.subscribe(`/topic/room/${room_id}`, function(sub_room_frame) {
                    create_new_conversation(JSON.parse(sub_room_frame.body))
                    latest_message(JSON.parse(sub_room_frame.body))
                    if(Notification.permission == 'granted'){
                        if (JSON.parse(sub_room_frame.body).chatMessageUserDto.userId != `${web_items_search.value}`
                            && document.visibilityState == 'hidden'){
                            switch (check_notification_message(JSON.parse(sub_room_frame.body))) {
                                case 'ON' :
                                    show_notification_message(JSON.parse(sub_room_frame.body))
                                    break;
                                default :
                                    break;
                            }
                        }
                    }
                    if(chat_input.hasAttribute('disabled')){
                        unblocked_from_user();
                        if(chat_screen.className){notification_trade_state.classList.add('hidden')}
                        chat_screen_and_room_height();
                        request_trade_state(chat_screen)
                    }
                },{id: `room-${room_id}`})
                sub_room_and_user = stomp_client.subscribe(`/topic/room/${room_id}/${web_items_search.value}`,function(sub_room_and_user_frame) {
                    read_message(JSON.parse(sub_room_and_user_frame.body))
                },{id: `room-user-${room_id}-${web_items_search.value}`})
            }else{
                create_conversation(JSON.parse(convo_frame.body))
                chat_screen.scrollTop = chat_screen.scrollHeight
                if(chat_screen.className.indexOf('welcome') > -1){
                    chat_room_empty.classList.add('hidden')
                    new_create_chat_list(JSON.parse(convo_frame.body)[0])
                    if(Notification.permission == 'granted') {
                        localStorage_notification_message(JSON.parse(convo_frame.body))
                        if(localStorage.getItem(`notification_list_no${web_items_search.value}`) != null){
                            const notification_list = JSON.parse(localStorage.getItem(`notification_list_no${web_items_search.value}`))
                            localStorage_new_notification_message(JSON.parse(convo_frame.body)[0], notification_list)
                            localStorage.setItem(`notification_list_no${web_items_search.value}`,JSON.stringify(notification_list))
                        }
                    }
                }
            }
        })
        stomp_client.subscribe(`/topic/chat/${web_items_search.value}`, function (sub_frame) {
            // 상대방으로부터 메시지가 올 때 기존 채팅목록에 최근 메시지 정보 최신화 및 새로운 채팅방 생성하는 곳
            latest_message(JSON.parse(sub_frame.body))
            has_chat_list();
            chat_list_opponent_nickname_width();
            chat_list_latest_message_width();
            send_count_height();
            // 상대방으로부터 메시지가 올 때 채팅 플로팅 버튼 옆에 읽지 않은 메시지 갯수 렌더링
            unread_total_chat();
            if (Notification.permission == 'granted'){
                if(document.visibilityState == 'hidden' ||
                    chat_screen.className && chat_screen.classList[1].slice(12) != JSON.parse(sub_frame.body).roomId){
                    switch (check_notification_message(JSON.parse(sub_frame.body))) {
                        case 'ON' :
                            show_notification_message(JSON.parse(sub_frame.body))
                            break;
                        default :
                            break;
                    }
                }
            }
        });
        stomp_client.subscribe('/user/queue/error', function (err_msg_frame) {
            chat_input.value = ''
            state_block_effect();
            chat_screen_and_room_height();
            if(JSON.parse(err_msg_frame.body).error_code == '1'){
                alert(`차단하신 ${chat_screen.classList[3].slice(9)}님과 채팅을 할 수 없습니다.`)
            }else{
                alert(`${chat_screen.classList[3].slice(9)}님에게 차단되어 채팅을 할 수 없습니다.`)
                blocked_from_user();
                trade_success_btn.setAttribute('disabled','')
                trade_success_text.style.color = '#adb5bd'
            }
        });
    },function (error) {
        console.log("[Connected Error] : " + error);
    })
}
// 웹소켓 연결 끊는 함수(옵션)
function websocket_disconnect() {
    if (stomp_client != null) {stomp_client.disconnect();}
    console.log("Disconnected");
}
// 채팅 메시지 보내는 함수
function send_message(msg,msg_type) {
    const message =
        {
            productId : `${chat_screen.classList[0].slice(7)}`,
            senderId : `${web_items_search.value}`,
            receiverId : `${chat_screen.classList[2].slice(11)}`,
            messageType : `${msg_type}`,
            message : `${msg}`
        }
    stomp_client.send("/message", {}, JSON.stringify(message))
}
// 채팅목록에서 채팅방으로 들어갈 때 구독하는 함수
function join_chat_room(click_chat_room) {
    sub_room = stomp_client.subscribe(`/topic/room/${click_chat_room.classList[1].slice(12)}` , function (join_sub_room_frame){
        create_new_conversation(JSON.parse(join_sub_room_frame.body))
        latest_message(JSON.parse(join_sub_room_frame.body))
        if(Notification.permission == 'granted'){
            if (JSON.parse(join_sub_room_frame.body).chatMessageUserDto.userId != `${web_items_search.value}`
                && document.visibilityState == 'hidden'){
                switch (check_notification_message(JSON.parse(join_sub_room_frame.body))) {
                    case 'ON' :
                        show_notification_message(JSON.parse(join_sub_room_frame.body))
                        break;
                    default :
                        break;
                }
            }
        }
        if(chat_input.hasAttribute('disabled')){
            unblocked_from_user();
            if(chat_screen.className){notification_trade_state.classList.add('hidden')}
            chat_screen_and_room_height();
            request_trade_state(chat_screen)
        }
    },{ id: `room-${click_chat_room.classList[1].slice(12)}`})
    sub_room_and_user = stomp_client.subscribe(`/topic/room/${click_chat_room.classList[1].slice(12)}/${web_items_search.value}`, function(join_sub_room_and_user_frame){
        read_message(JSON.parse(join_sub_room_and_user_frame.body))
    },{id: `room-user-${click_chat_room.classList[1].slice(12)}-${web_items_search.value}`})
}
// 채팅방에서 뒤로 가거나 채팅창을 닫을 때 채팅방에 대한 구독을 취소하는 함수
function unsubscribe_chat_room(){
    if (stomp_client != null){
        sub_room.unsubscribe();
        sub_room_and_user.unsubscribe();
    }
}