/**
 * 샘플 웹소켓 클라이언트 html
 * - 수정내용 (1.25)
 * 기존의 구독에서 2개 추가 되었음
 * 1. sub user/queue/room/event
 * 2. sub user/queue/error
 *
 */

var stompClient = null;
var _room_id = null;
var toggle = false;
var sub_room = null;
var sub_room_and_user = null;
var sub_room_event = null;
const message_container = document.getElementById('chat_message');
var test_result = null;
let alarm_list = null;


window.addEventListener("beforeunload", function (e) {
    return sub_room_and_user.unsubscribe("room-user");
});


// 알람 허용유무 조사
if(Notification.permission === "granted"){
    console.log("이미 알람 허용된상태")
}else if(Notification.permission !== "denied"){ //
    Notification.requestPermission().then(permission => { // 알람 허용 유무 묻기
        if(permission === "granted"){
            console.log("알람 허용")
        }
    });
}

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

// 최초 로그인후 웹소켓 연결하기 (STOMP API 1번)
function connect(userid) {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {


        // 채팅방구독시 채팅방의 메시지들을 받을 구독
        sub_room_event = stompClient.subscribe('/user/queue/room/event', function (frame) { // 추가-2 SUB
            console.log(JSON.parse(frame.body)) // 채팅메시지들을 받게되는 부분


            // 물건 페이지에서 [채팅으로 거래하기] 누른 후 채팅방에서 메시지를 보냈을 떄(`SEND /message`) 받게 되는 메시지 형태
            // 서버로부터 채팅방에 대한 정보를 받는다.
            // 서버로 부터 채팅방 정보를 받는 경우 ( {"roomId":"101"} )
            if (Object.keys(JSON.parse(frame.body)).length === 1 && toggle === false){
                toggle = true;
                _room_id = JSON.parse(frame.body)['roomId']
                sub_room = stompClient.subscribe('/topic/room/' + _room_id, function (f) { // {"roomId":"101"} 이용 -> ( STOMP API 3번, 채팅방에 대해 구독)
                    console.log(JSON.parse(f.body)) // 실시간으로 오는 메시지 받아서 DOM 에 그려야 하는 부분
                    if (document.visibilityState === "hidden"){ // 유저가 화면을 띄우지 않았을 경우(다른 텝으로 이동)
                        if(Notification.permission === "granted"){
                            switch (is_alarm(f)){
                                case 1: // on
                                    console.log("알림이 켜져있습니다.")
                                    showNotification(f);
                                    break;
                                case 2: // off
                                    console.log("알림이 꺼져있습니다.")
                                    break;
                                case 3: // 아예 없는 경우
                                    console.log("새로운 알림입니다.")
                                    showNotification(f);
                                    break;
                            }
                        }
                    }

                },{ id: "room-" + _room_id}); // header


                sub_room_and_user = stompClient.subscribe('/topic/room/' + _room_id + '/' + userid, function (f) {
                    console.log(JSON.parse(f.body));
                },{ id: "room-user-" + _room_id + "-" + userid}); // header



                $("#leave_btn").prop("disabled", false)
                $("#join_btn").prop("disabled", true)
            }
        });

        // 에러메시지를 담고있는 MESSAGE Frame 을 받을 구독
        stompClient.subscribe('/user/queue/error', function (frame) {
            console.log(frame);
        });


        // getUnreadMessages() // 유저의 읽지 않은 메시지 개수 가져오기 (HTTP REST API, 오로지 ws 관련 테스트를 진행하고 싶다면 주석처리 하면됨// )


        setConnected(true);
        console.log('Connected: ' + frame);


        stompClient.subscribe('/topic/chat/' + userid, function (frame) { // (STOMP API 2번) 나에게 오는 메시지 받기, 구독
            console.log(frame);
            // 1번과 2번이 아닌 다른 10번유저가 1번에게 메시지를 보내는 경우
            var msgbody = JSON.parse(frame.body);
            if (document.visibilityState === "hidden" || ((msgbody.roomId !== _room_id) && _room_id !== null)){
                if(Notification.permission === "granted"){
                    switch (is_alarm(frame)){
                        case 1: // on
                            console.log("알림이 켜져있습니다.")
                            showNotification(frame);
                            break;
                        case 2: // off
                            console.log("알림이 꺼져있습니다.")
                            break;
                        case 3: // 아예 없는 경우
                            console.log("새로운 알림입니다.")
                            showNotification(frame);
                            break;
                    }
                }
            }
        });

    },function (error) {
        // 서버에서 어떤 예외 또는 에러가 발생했는 지 에러메시지 확인 할 수 있다.
        // error 프레임을 받는다!!
        console.log("[Connected Error] : " + error);
        console.log(error)
        console.log(error.headers)
        console.log(error.headers.message)
        var socket1 = new SockJS('/ws');
        var stompClient1 = Stomp.over(socket1);

        // error 프레임을 어떻게 처리할 것인가!
        stompClient1.connect({}, function () {
            getUnreadMessages() // 유저의 읽지 않은 메시지 개수 가져오기 (HTTP REST API)
            stompClient1.subscribe('/topic/chat/' + userid, function (frame){
                console.log(frame)
            })
        }, function (error1) {
            console.log("다시연결!! : " + error1)
        })
    });
}

/**
 * 연결끊기
 */
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}


/**
 * @param userid 유저 아이디
 * @param pid 물건아이디
 * @param reciId 받는이 아이디
 */
function sendMessage(userid, pid, reciId) {
    var message =
        {
            productId : pid, // 물건 아이디
            senderId : userid, // 보내는 이 고유 아이디
            receiverId : reciId, // 받는 이 고유 아이디
            messageType : 0, // 메시지 타입
            message : $("#name").val() // 메시지내용
        }
    stompClient.send("/message", {}, JSON.stringify(message))
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}


// [채팅방 목록 -> 채팅방 화면]
function join(room_id, userid){

    if(toggle === false){
        toggle = true
        _room_id = room_id;
        sub_room = stompClient.subscribe('/topic/room/' + room_id , function (fr){ // (STOMP API 3번) 채팅방 구독하기
            console.log(JSON.parse(fr.body)) // 메시지 받아서 DOM 에 그려야 하는 부분


            if (document.visibilityState === "hidden"){ // 유저가 화면을 띄우지 않았을 경우(다른 텝으로 이동)
                if(Notification.permission === "granted"){ // 알람승인
                    switch (is_alarm(fr)){
                        case 1: // on
                            console.log("알림이 켜져있습니다.")
                            showNotification(fr);
                            break;
                        case 2: // off
                            console.log("알림이 꺼져있습니다.")
                            break;
                        case 3: // 아예 없는 경우
                            console.log("새로운 알림입니다.")
                            showNotification(fr);
                            break;
                    }
                }
            }

        },{ id: "room-" + room_id}) //

        sub_room_and_user = stompClient.subscribe('/topic/room/' + room_id + '/' + userid, function(f){ // (수정)상대방입장했을 때의 이벤트{join}만 받는다.
            console.log(JSON.parse(f.body))
        },{id: "room-user-" + room_id + "-" + userid})


        $("#join_btn").prop("disabled", true)
        $("#leave_btn").prop("disabled", false)
    }
}

// 채팅방 나가기
function leaveRoom(leave){
    $("#join_btn").prop("disabled", !(leave))
    $("#leave_btn").prop("disabled", leave)
    sub_room.unsubscribe(); // (STOMP API 6번) 채팅방에대한 구독 취소
    sub_room_and_user.unsubscribe(); // (STOMP API 6번) (채팅방 + 유저(나))에대한 구독 취소
    _room_id = null;
    toggle = false;
}


$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#product_page_chat_button").click(function(){ checkRoomExistd($("#userid").val(), $("#pid").val(), $("#reci-id").val())})
    $( "#connect" ).click(function() { connect($("#userid").val()); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendMessage($("#userid").val(), $("#pid").val(), $("#reci-id").val())});
    $( "#chat_floating_btn" ).click(function() { getRoomAndMessages($("#userid").val())});
    $("#join_btn").click(function () { join($("#room-id").val(), $("#userid").val(), true); });
    $("#leave_btn").click(function (){ leaveRoom(true)});
    $("#upload_button").click( function () { upload(document.getElementById('upload'))});
    $("#alarm_button").click(function (){alarm_onoff(document.getElementById('alarm_input').value)});
});


// 채팅방 목록 조회 API
function getRoomAndMessages(userId){
    var result = "";
    $.ajax({
        cache : false,
        // gateway -> 통과해서 테스트한다면 포트번호를 바꿔야할 것이다.
        url : "http://localhost:8080/chatroom/lists",
        type : 'GET',
        async : false,
        // header 에 token 추가
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0YWVrd29uQGRvbmdndWsuZWR1IiwiaWF0IjoxNjE0MDQ2NTQ3LCJleHAiOjE2MTQxNjY1NDd9.CWqdFKY4u-dSgiwueZJZGFnMEHmVDClrjSumc25cTLewnr-CX-Y_5eEQhkoDdYlA6rRN1DkU1eT4LwRWSF34oA'
        },
        success : function(data) {
            // console.log('get rooms and messages : ' ,data)
            result = data
            test_result = data;

            if(localStorage.getItem('alarm_list') == null) {
                var alarm_list = [];
                for(var i in test_result) {
                    var jsonObject = {};
                    jsonObject.room_id = test_result[i].roomId;
                    jsonObject.alarm = "on";
                    alarm_list.push(jsonObject);
                    console.log(alarm_list)
                    localStorage.setItem("alarm_list", JSON.stringify(alarm_list))
                }
            }


        }, // success
        error : function(xhr, status) {
            alert(xhr + " : " + status);
        }
    }); // $.ajax */
    return result;
}


/**
 * 서버로 부터 읽지 않은 채팅 메시지들의 개수를 가져와 리턴합니다
 *
 * @param userId 유저 아이디
 * @returns 읽지 않은 메시지의 개수
 */
function getUnreadMessages(){
    var result = "";
    $.ajax({
        cache : false,
        // gateway -> 통과해서 테스트한다면 포트번호를 바꿔야할 것이다.
        url : "http://localhost:8080/chat/unread/messages", // 요기에
        type : 'GET',
        async : false,
        // header 에 token 추가
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0YWVrd29uQGRvbmdndWsuZWR1IiwiaWF0IjoxNjE0MDQ2NTQ3LCJleHAiOjE2MTQxNjY1NDd9.CWqdFKY4u-dSgiwueZJZGFnMEHmVDClrjSumc25cTLewnr-CX-Y_5eEQhkoDdYlA6rRN1DkU1eT4LwRWSF34oA'
        },
        success : function(data) {
            result = data;
            console.log('get unread messages : ', result)
        }, // success

        error : function(xhr, status) {
            alert(xhr + " : " + status);
        }
    });
    return result;
}

// 알림 띄어주기
function showNotification(message){ // topic/chat/{user-id} 로 오는 유저 메시지들
    const msgBody = JSON.parse(message.body)

    var n = new Notification(" 동국마켓 : " + msgBody.chatMessageUserDto.nickName, {
        body: msgBody.message,
        icon: msgBody.chatRoomProductDto.productImgPath,
        tag: msgBody.roomId
    });
}


function alarm_onoff(room_id) {
    console.log("선택한 방번호 : " + room_id)
    var alarm_list = JSON.parse(localStorage.getItem("alarm_list"))


    var isOn = alarm_list.filter(x => x.room_id === parseInt(room_id)).map(x => x.alarm)
    console.log("isOn : " + isOn)
    if(isOn == "off"){
        // off
        console.log("1")
        for (var i = 0; i < alarm_list.length; i++) {
            if(alarm_list[i].room_id === parseInt(room_id)){  //look for match with room_id
                console.log("2")
                alarm_list[i].alarm = "on";
                break;  //exit loop since you found the room
            }
        }
        localStorage.setItem("alarm_list", JSON.stringify(alarm_list));  //put the object back
    }else if (isOn == "on"){
        // on
        for (var i = 0; i < alarm_list.length; i++) {
            if(alarm_list[i].room_id === parseInt(room_id)){  //look for match with room_id
                alarm_list[i].alarm = "off"
                break;  //exit loop since you found the room
            }
        }
        localStorage.setItem("alarm_list", JSON.stringify(alarm_list));  //put the object back
    }
}

// 알람 on/off 체크하기
function is_alarm(frame) {
    var alarm_list = JSON.parse(localStorage.getItem("alarm_list"))
    var _isOn = alarm_list.filter(x => x.room_id === parseInt(JSON.parse(frame.body).roomId)).map(x => x.alarm);

    console.log("roomId : " + JSON.parse(frame.body).roomId);
    console.log("is on? : " + alarm_list.filter(x => x.room_id === parseInt(JSON.parse(frame.body).roomId)).map(x => x.alarm))
    console.log("_isOn : " + _isOn)
    console.log("_isOn === on? " + _isOn === 'on')
    console.log("_isOn === on? " + _isOn === 'off')
    if(_isOn == "on"){
        console.log("알람기능 ON")
        return 1;
    }else if (_isOn == "off"){
        console.log("알람기능 OFF")
        return 2;
    }else{ // 없는 경우
        var jsonObject = {};
        jsonObject.room_id = parseInt(JSON.parse(frame.body).roomId)
        jsonObject.alarm = "on"
        alarm_list.push(jsonObject)
        localStorage.setItem("alarm_list", JSON.stringify(alarm_list));
        return 3;
    }
}