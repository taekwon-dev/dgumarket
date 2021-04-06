package com.springboot.dgumarket.service.chat;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.devicefarm.model.Run;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.xray.model.Http;
import com.springboot.dgumarket.dto.chat.ChatMessageDto;
import com.springboot.dgumarket.dto.chat.ChatMessageUserDto;
import com.springboot.dgumarket.dto.chat.ChatRoomProductDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.chat.RedisChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.response.stomp.StompRoomInfo;
import com.springboot.dgumarket.repository.chat.ChatMessageRepository;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.chat.RedisChatRoomRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatImageStorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    AmazonS3 s3Client;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    RedisChatRoomRepository redisChatRoomRepository;

    @Autowired
    SimpMessagingTemplate template;

    @Autowired
    ModelMapper modelMapper;

    /**
     * 유저로부터 이미지파일을 받아 이미지를 업로드 합니다.
     * @param multipartFile 사용자로 부터 받은 이미지파일들
     * @param senderId 보내는 이 아이디
     * @param receiverId 받는 이 아이디
     * @param productId 물건 아이디
     * @param sessionId 유저의 웹소켓 세션 아이디
     * @return 업로드 성공 유무
     */

    public List<String> chatUploadImages(
            MultipartFile[] multipartFile,
            String uploadDirPrefix,
            String sessionId,
            int senderId,
            int receiverId,
            int productId) throws CustomControllerExecption, IOException {


        List<String> fileStrPathList = new ArrayList<>();

        Member sender = memberRepository.findById(senderId);
        Member receiver = memberRepository.findById(receiverId);
        Product product = productRepository.getOne(productId);

        // 예외처리
        if(receiver == null || receiver.getIsWithdrawn()==1){ // 탈퇴한 유저에게 채팅이미지를 전송하려고 하는 경우
            throw new CustomControllerExecption("탈퇴한 유저입니다.", HttpStatus.NOT_FOUND);
        }
        if(receiver.getIsEnabled() == 1){ // 관리자로 부터 이용제재 당하고 있는 경우
            throw new CustomControllerExecption("관리자로 부터 제재당하고 있는 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if(sender.getBlockUsers().contains(receiver)){ // 내가 상대방을 차단
            throw new CustomControllerExecption("차단한 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if(sender.getUserBlockedMe().contains(receiver)){ // 상대방이 나를 차단
            throw new CustomControllerExecption("차단당한 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        PropertyMap<Product, ChatRoomProductDto> productMap = new PropertyMap<Product, ChatRoomProductDto>() {
            @Override
            protected void configure() {
                map().setProduct_id(source.getId());
                map().setProduct_deleted(source.getProductStatus());
                map().setProductImgPath(source.getImgDirectory());
            }
        };
        PropertyMap<Member, ChatMessageUserDto> userDtoPropertyMap = new PropertyMap<Member, ChatMessageUserDto>() {
            @Override
            protected void configure() {
                map().setUserId(source.getId());
                map().setNickName(source.getNickName());
                map().setProfileImgPath(source.getProfileImageDir());
            }
        };

        TypeMap<Product, ChatRoomProductDto> typeMap = modelMapper.getTypeMap(Product.class, ChatRoomProductDto.class);
        TypeMap<Member, ChatMessageUserDto> userDtoTypeMap = modelMapper.getTypeMap(Member.class, ChatMessageUserDto.class);
        if(typeMap == null){
            modelMapper.addMappings(productMap);
        }

        if(userDtoTypeMap == null){
            modelMapper.addMappings(userDtoPropertyMap);
        }

        // get roomId
        ChatRoom chatRoom = chatRoomRepository.findChatRoomPSR(productId, senderId, receiverId);
        if (chatRoom == null) {
            chatRoom = chatRoomRepository.save(ChatRoom.builder() // 채팅방 만들고 저장
                    .consumer(memberRepository.findById(senderId))
                    .seller(memberRepository.findById(receiverId))
                    .product(productRepository.getOne(productId)).build());

            log.info("채팅방 없어서 채팅방 만듬");
            if(multipartFile != null){
                log.info("multipartFile.length : {}", multipartFile.length);
            }
            log.info("uploadDirPrefix : {} ", uploadDirPrefix);
            log.info("sessionId : {}", sessionId);
            log.info("multipartFile : {} , uploadDirPrefix : {} , chatRoom : {}, sender : {}, receiver : {}, product : {}",
                    multipartFile, uploadDirPrefix, chatRoom.getRoomId(), sender.getId(), receiver.getId(), product.getId());

            fileStrPathList = uploadFileAndSendMessage(multipartFile, uploadDirPrefix, chatRoom, sender, receiver, product); // s3 업로드 & 메시지보내기
            log.info("fileStrPathList : {}", fileStrPathList);
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(sessionId);
            headerAccessor.setLeaveMutable(true);
            StompRoomInfo stompRoomInfo = new StompRoomInfo(String.valueOf(chatRoom.getRoomId()));
            template.convertAndSendToUser(sessionId,"/queue/room/event", stompRoomInfo, headerAccessor.getMessageHeaders()); // 채팅방 정보 전달
            return fileStrPathList;
        }

        log.info("찾은 채팅방 : {}", chatRoom.getRoomId());

        // 채팅방에 사용자 있는 지 유무 확인
        RedisChatRoom redisChatRoom = redisChatRoomRepository.findById(String.valueOf(chatRoom.getRoomId())).orElse(null);
        assert redisChatRoom != null;
        if(redisChatRoom.isSomeoneInChatRoom(String.valueOf(receiverId))){
            log.info("chatRoom: {}, sender:{}, receiver:{}, product:{}",
                    chatRoom.getRoomId(), sender.getId(), receiver.getId(), product.getId());
            fileStrPathList = uploadFileAndSendMessage(multipartFile, uploadDirPrefix, chatRoom, sender, receiver, product); // s3 업로드 & 메시지보내기
        }else { // another person not in room
            log.info("chatRoom: {}, sender:{}, receiver:{}, product:{}",
                    chatRoom.getRoomId(), sender.getId(), receiver.getId(), product.getId());
            fileStrPathList = uploadFileAndSendMessage(multipartFile, uploadDirPrefix, chatRoom, sender, receiver, product); // s3 업로드 & 메시지보내기
        }
        return fileStrPathList;
    }


    /**
     * 유저로 부터 받은 파일들을 s3로 업로드 하고 상대방(채팅방) 으로 메시지(STOMP)를 보낸다.
     * 다만 10개를 업로드 할 경우 한개가 업로드 될 때마다 특정 상대방(채팅방)에 메시지를 전달한다.
     * 즉, 10개의 파일을 모두 업로드 완료한 후에 메시지(STOMP)를 보내지 않는 다는 뜻.
     *
     * @param multipartFiles 업로드요청받은 파일들
     * @param chatRoom 채팅방정보
     * @param sender 보내는이
     * @param receiver 받는이
     * @param product 물건정보
     */
    public List<String> uploadFileAndSendMessage(
                                         MultipartFile[] multipartFiles,
                                         String uploadDirPrefix,
                                         ChatRoom chatRoom,
                                         Member sender,
                                         Member receiver,
                                         Product product) throws CustomControllerExecption, IOException {


        // init
        String fileType = null; // 각 사진의 파일타입
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로

        List<String> fileNameLists = new ArrayList<>();

        log.info("여기가 실행 됨");


        for (int i = 0; i < multipartFiles.length; i++) {

            log.info("{} 번째 이미지 보내는 중 -> 상대방 탈퇴유무 : {}", i,receiver.getIsWithdrawn());
            log.info("{} 번째 이미지 보내는 중 -> 상대방 관리자이용제재유무 : {}", i, receiver.getIsWithdrawn());
            // [나]내가 탈퇴 / 유저제재 당한 경우도 체크해야함. 이 부분까지 온 상황에서는 게이트웨이가 검사할 수 없다.
            if(sender == null || sender.getIsWithdrawn() == 1){ // 내가 탈퇴했을 경우
                throw new CustomControllerExecption("탈퇴하였습니다.", HttpStatus.NOT_FOUND);
            }

            if(sender.getIsEnabled() == 1){ // 내가 이용제재 당하는 경우
                throw new CustomControllerExecption("관지라로부터 이용제재 받고 있습니다. 서비스를 이용하실 수 없습니다.", HttpStatus.BAD_REQUEST);
            }


            // [상대방]쓸데 없는 이미지 저장을 막기위해 미리 상대방의 차단/유저제재/탈퇴 유무를 검사합니다.
            if(receiver == null || receiver.getIsWithdrawn() == 1){ // 상대방이 탈퇴했을 경우
                throw new CustomControllerExecption("탈퇴한 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            if(receiver.getIsEnabled() == 1){ // 상대방이 이용제재 받고 있을 경우
                throw new CustomControllerExecption("관리자로 부터 이용제재 받고 있는 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.NOT_FOUND);
            }

            if(sender.getBlockUsers().contains(sender)){ // 내가 상대방 차단
                throw new CustomControllerExecption("차단한 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
            }

            if(sender.getUserBlockedMe().contains(sender)){ // 상대방이 나를 차단
                throw new CustomControllerExecption("차단당한 유저에게 메시지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
            }



            log.info("파일 이름 : {}", multipartFiles[i].getOriginalFilename());

            // 파일명 생성
            // 업로드 디렉토리
            // 파일타입
            fileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);
            fileName = UUID.randomUUID().toString().replace("-", "") + "." + fileType;
            uploadDirOnS3 = uploadDirPrefix + fileName;
            log.info("fileNamve : {}", fileName);
            log.info("uploadDirOnS3 : {}", uploadDirOnS3);

            // 반환 할 파일명 리스트를 위해서 파일명 생성 후 리스트에 추가.
            fileNameLists.add(fileName);
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(multipartFiles[i].getContentType());
                metadata.setContentLength(multipartFiles[i].getSize());
                metadata.setHeader("filename", fileName);

                // s3 multipart upload
                TransferManager tm = TransferManagerBuilder.standard()
                        .withS3Client(s3Client)
                        .build();

                // TransferManager processes all transfers asynchronously,
                // so this call returns immediately.
                Upload upload = tm.upload(bucketName, uploadDirOnS3, multipartFiles[i].getInputStream(), metadata);

                // Optionally, wait for the upload to finish before continuing.
                upload.waitForCompletion();

                if (upload.getState() == Transfer.TransferState.Completed) {

                    log.info("이미지 전송 완료!!, fileName: {}, chatRoom: {}, sender:{}, receiver:{}, product:{}",
                            fileName, chatRoom.getRoomId(), sender.getId(), receiver.getId(), product.getId());
                    int inRoom = isInRoom(chatRoom.getRoomId(), receiver.getId()); // 채팅방에 사람이 있는 지 확인
                    ChatMessage chatMessage = saveMessage(inRoom, fileName, chatRoom, sender, receiver, product); // 메시지저장
                    log.info("저장된 chatMessage : {}",chatMessage.toString());
                    sendMessage(chatMessage, inRoom); // 메시지 전송
                }

            } catch (FileNotFoundException | InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                log.info("IOExcetion 발생 통과시키기");
                continue;
//                continue;
//                e.printStackTrace();
            }
        }

        return fileNameLists; // 저장 파일리스트
    }



    /**
     * 상대방에게(혹은 채팅방)메시지를 보낸다
     * @param inRoom 상대방이 방에 존재하는 지 여부
     * @param key s3 object key
     * @param chatRoom chatroom
     * @param sender 보내는 이
     * @param receiver 받는 이
     * @param product 물건 정보
     */
    public void sendMessage(
            boolean inRoom,
            String key,
            ChatRoom chatRoom,
            Member sender,
            Member receiver,
            Product product){

        // 메시지 저장
        URL s3Url = s3Client.getUrl(bucketName, key);
        ChatMessage chatMessage;
        if (inRoom) {
            chatMessage = chatMessageRepository.save(ChatMessage.builder()
                    .roomId(chatRoom.getRoomId())
                    .sender(sender)
                    .receiver(receiver)
                    .msgStatus(1) // read
                    .msgType(1)
                    .product(product)
                    .message(s3Url.toString()).build());
        }else {
            chatMessage = chatMessageRepository.save(ChatMessage.builder()
                    .roomId(chatRoom.getRoomId())
                    .sender(sender)
                    .receiver(receiver)
                    .msgStatus(0) // unread
                    .msgType(1)
                    .product(product)
                    .message(s3Url.toString()).build());
        }

        // dto 변환
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setRoomId(chatMessage.getRoomId());
        chatMessageDto.setChatMessageUserDto(modelMapper.map(chatMessage.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
        chatMessageDto.setChatRoomProductDto(modelMapper.map(chatMessage.getProduct(), ChatRoomProductDto.class));
        chatMessageDto.setMessageDate(chatMessage.getMsgDate());
        chatMessageDto.setMessage(chatMessage.getMessage());
        chatMessageDto.setMessage_type(chatMessage.getMsgType());
        chatMessageDto.setMessageStatus(chatMessage.getMsgStatus());

        if (inRoom){
            // send message to room
            template.convertAndSend("/topic/room/" + chatRoom.getRoomId(), chatMessageDto);
        }else{

            // send message user
            template.convertAndSend("/topic/chat/" + receiver.getId(), chatMessageDto);
        }
    }

    // 메시지 전송
    public void sendMessage(ChatMessage chatMessage, int isInRoom){
        log.info("sendMessage 실행?");
        // dto 변환
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setRoomId(chatMessage.getRoomId());
        chatMessageDto.setChatMessageUserDto(modelMapper.map(chatMessage.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
        chatMessageDto.setChatRoomProductDto(modelMapper.map(chatMessage.getProduct(), ChatRoomProductDto.class));
        chatMessageDto.setMessageDate(chatMessage.getMsgDate());
        chatMessageDto.setMessage(chatMessage.getMessage());
        chatMessageDto.setMessage_type(chatMessage.getMsgType());
        chatMessageDto.setMessageStatus(chatMessage.getMsgStatus());

        log.info("만든 채팅 메시지 dto : {}", chatMessageDto.toString());
        log.info("isInRoom : {}", isInRoom);
        // 메시지 전송
        if(isInRoom==1){ // 채팅방에 있을 경우
            log.info("/topic/room/{}", chatMessage.getRoomId());
            template.convertAndSend("/topic/room/" + chatMessage.getRoomId(), chatMessageDto);
        }else if(isInRoom==2){ // 채팅방에 없을 경우
            log.info("/topic/chat/{}", chatMessage.getReceiver().getId());
            template.convertAndSend("/topic/chat/" + chatMessage.getReceiver().getId(), chatMessageDto);
            template.convertAndSend("/topic/room/" + chatMessage.getRoomId(), chatMessageDto);
        }else if(isInRoom==3){ // 새로운 채팅방을 개설한 경우
            template.convertAndSend("/topic/chat/" + chatMessage.getReceiver().getId(), chatMessageDto);
        }
    }


    // 메시지 저장
    public ChatMessage saveMessage(int isInRoom, String imgPath, ChatRoom chatRoom, Member sender, Member receiver, Product product){
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(chatRoom.getRoomId())
                .sender(sender)
                .receiver(receiver)
                .product(product)
                .msgType(1)
                .message(imgPath).build();
        if (isInRoom==1) {
            chatMessage.setMsgStatus(1);
        }else if(isInRoom==2 || isInRoom==3){
            chatMessage.setMsgStatus(0);
        }
        return chatMessageRepository.save(chatMessage);
    }

    // 메시지 전송 시점에 상대방이 채팅방에 있는 지 없는 지 확인합니다.
    public int isInRoom(int chatRoomId, int receiverId){
        // 채팅방에 사용자 있는 지 유무 확인
        RedisChatRoom redisChatRoom = redisChatRoomRepository.findById(String.valueOf(chatRoomId)).orElse(null);

        // 만약 레디스에 아무도 안들어왔다? ( -> 새로운 채팅방생성)
        if(redisChatRoom==null){
            return 3;
        }

        if(redisChatRoom.isSomeoneInChatRoom(String.valueOf(receiverId))){
            log.info("상대방이 채팅방에 있네?");
            return 1;
        }else { // another person not in room
            log.info("상대방이 채팅방에 없네?");
            return 2;
        }
    }
}
