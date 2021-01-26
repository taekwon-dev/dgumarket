/**
 * 샘플 웹소켓 클라이언트 html
 * - 수정내용 (1.25)
 * 기존의 구독에서 2개 추가 되었음
 * 1. sub user/queue/room/event
 * 2. sub user/queue/error
 *
 */


var stompClient = null;
var room_id = null;
var toggle = false;
var sub_room = null;
var sub_room_and_user = null;
var sub_room_event = null;
const message_container = document.getElementById('chat_message');

window.addEventListener("beforeunload", function (e) {
    return sub_room_and_user.unsubscribe("room-user");
});

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
            console.log(JSON.parse(frame.body)) // 채팅메시지들을 받게되는 부분.


            // 물건 페이지에서 [채팅으로 거래하기] 누른 후 채팅방에서 메시지를 보냈을 떄(`SEND /message`) 받게 되는 메시지 형태
            // 서버로부터 채팅방에 대한 정보를 받는다.
            // 서버로 부터 채팅방 정보를 받는 경우 ( {"roomId":"101"} )
            if (Object.keys(JSON.parse(frame.body)).length === 1 && toggle === false){
                toggle = true;
                room_id = JSON.parse(frame.body)['roomId']

                sub_room = stompClient.subscribe('/topic/room/' + room_id, function (f) { // {"roomId":"101"} 이용 -> ( STOMP API 3번, 채팅방에 대해 구독)
                    console.log(JSON.parse(f.body)) // 실시간으로 오는 메시지 받아서 DOM 에 그려야 하는 부분

                },{ id: "room-" + room_id}); // header


                sub_room_and_user = stompClient.subscribe('/topic/room/' + room_id + '/' + userid, function (f) {

                    console.log(JSON.parse(f.body))

                },{ id: "room-user-" + room_id + "-" + userid}); // header




                $("#leave_btn").prop("disabled", false)
                $("#join_btn").prop("disabled", true)
            }
        });

        // 에러메시지를 담고있는 MESSAGE Frame 을 받을 구독
        stompClient.subscribe('/user/queue/error', function (frame) {
            console.log(frame);
        });


        getUnreadMessages() // 유저의 읽지 않은 메시지 개수 가져오기 (HTTP REST API)


        setConnected(true);
        console.log('Connected: ' + frame);


        stompClient.subscribe('/topic/chat/' + userid, function (frame) { // (STOMP API 2번) 나에게 오는 메시지 받기, 구독



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

        sub_room = stompClient.subscribe('/topic/room/' + room_id , function (fr){ // (STOMP API 3번) 채팅방 구독하기
            console.log(JSON.parse(fr.body)) // 메시지 받아서 DOM 에 그려야 하는 부분
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
});


/**
 * userId가 대화하고 있는 채팅방 정보(관련된 메시지들 포함)를 가져와 리턴합니다
 *
 * @param userId 사용자 아이디
 * @returns userId가 대화하고 있는 채팅방 정보(관련된 메시지들 포함)
 */
function getRoomAndMessages(userId){
    var result = "";
    $.ajax({
        cache : false,
        url : "http://localhost:8080/chat/rooms",
        type : 'GET',
        async : false,
        success : function(data) {
            console.log('get rooms and messages : ' ,data)
            result = data
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
        url : "http://localhost:8080/chat/user/unread/messages", // 요기에
        type : 'GET',
        async : false,
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

