package com.springboot.dgumarket.service.block;

import com.springboot.dgumarket.dto.block.BlockStatusDto;


public interface UserBlockService {
    boolean blockUser(int userId, int blockUserId); // 차단하기
    void unBlockUser(int userId, int unblockUserId); // 차단 해제
    BlockStatusDto checkBlockStatus(int userId, int targetUserId); // 유저차단상태(서로)
}
