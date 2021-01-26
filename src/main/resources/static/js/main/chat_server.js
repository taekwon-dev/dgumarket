let stomp_client = null;
let sub_room = null;
let sub_room_and_user = null;

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
                total_alm.classList.add('hidden')
            }else{
                total_alm.classList.remove('hidden')
                if(Number(data.data.unreadMessagesCnt) <= 99){
                    total_alm.innerText = `${data.data.unreadMessagesCnt}`
                }else{
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
            create_chat_list(data);
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
            console.log(data);
            trade_success_view()
            chat_screen_and_room_height()
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
            trade_state(data)
            chat_screen_and_room_height()
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
function request_chat_room_out(from_chat_list) {
    let room_id;
    if(chat_list_form.className.indexOf('hidden') == -1){
        room_id = from_chat_list.slice(27)
    }else if(chat_screen.classList[4]){
        room_id = chat_screen.classList[4].slice(12)
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
        })
        .catch(error => {
            console.log(error)
        })
}
//웹소켓 연결 및 구독하는 함수
function websocket_connect() {
    let socket = new SockJS('/ws');
    stomp_client = Stomp.over(socket);
    stomp_client.connect({}, function(connect_frame) {
        console.log('connected: ' + connect_frame)
        // 웹소켓 연결 시 구독까지 진행되며 send_message()실행 후 콜백함수 코드 브라우저에 렌더링
        stomp_client.subscribe(`/topic/chat/${web_items_search.value}`, function (sub_frame) {
            //채팅으로 거래하기 클릭하여 send_message()실행 후 아래와 같은 방법으로 구독 실시
            if(Object.keys(JSON.parse(sub_frame.body)).length == 1){
                const room_id = JSON.parse(sub_frame.body).roomId
                sub_room = stomp_client.subscribe(`/topic/room/${room_id}`, function(sub_room_frame) {
                    console.log('sub_room_frame: ' + sub_room_frame)
                    create_new_conversation(JSON.parse(sub_room_frame.body))
                },{id: `room-${room_id}`})
                sub_room_and_user = stomp_client.subscribe(`/topic/room/${room_id}/${web_items_search.value}`,function(sub_room_and_user_frame) {
                    console.log('sub_room_and_user_frame: ' + sub_room_and_user_frame)
                    chat_screen.classList.add(`chat_list_no${room_id}`)
                    close_chat_room_form.classList.remove('hidden')
                    create_conversation(JSON.parse(sub_room_and_user_frame.body))
                    read_message(JSON.parse(sub_room_and_user_frame.body))
                },{id: `room-user-${room_id}-${web_items_search.value}`})
            }else{
                // 상대방으로부터 메시지가 올 때 채팅목록에 최근 메시지 정보를 렌더링
                latest_message(JSON.parse(sub_frame.body))
                has_chat_list();
                chat_list_opponent_nickname_width();
                chat_list_latest_message_width();
                send_count_height();
                // 상대방으로부터 메시지가 올 때 채팅 플로팅 버튼 옆에 읽지 않은 메시지 갯수 렌더링
                unread_total_chat()
            }
        })
    },function (error) {
        console.log("[Connected Error] : " + error);
    })
}
// 웹소켓 연결 끊는 함수(옵션)
function websocket_disconnect() {
    if (stomp_client !== null) {stomp_client.disconnect();}
    console.log("Disconnected");
}
// 채팅 메시지 보내는 함수
function send_message() {
    const message =
        {
            productId : `${chat_screen.classList[0].slice(7)}`,
            senderId : `${web_items_search.value}`,
            receiverId : `${chat_screen.classList[2].slice(11)}`,
            messageType : `${0}`,
            message : chat_input.value
        }
    stomp_client.send("/message", {}, JSON.stringify(message))
}
// 채팅목록에서 채팅방으로 들어갈 때 구독하는 함수
function join_chat_room(click_chat_room) {
    sub_room = stomp_client.subscribe(`/topic/room/${click_chat_room.classList[1].slice(12)}` , function (join_sub_room_frame){
        console.log(JSON.parse(join_sub_room_frame.body))
        create_new_conversation(JSON.parse(join_sub_room_frame.body))
    },{ id: `room-${click_chat_room.classList[1].slice(12)}`})
    sub_room_and_user = stomp_client.subscribe(`/topic/room/${click_chat_room.classList[1].slice(12)}/${web_items_search.value}`, function(join_sub_room_and_user_frame){
        console.log(JSON.parse(join_sub_room_and_user_frame.body))
        create_conversation(JSON.parse(join_sub_room_and_user_frame.body))
        read_message(JSON.parse(join_sub_room_and_user_frame.body))
    },{id: `room-user-${click_chat_room.classList[1].slice(12)}-${web_items_search.value}`})
}
// 채팅방에서 뒤로 가거나 채팅창을 닫을 때 채팅방에 대한 구독을 취소하는 함수
function unsubscribe_chat_room(){
    if (stomp_client !== null){
        sub_room.unsubscribe();
        sub_room_and_user.unsubscribe();
    }
}