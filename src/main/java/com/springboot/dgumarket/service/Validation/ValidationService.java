package com.springboot.dgumarket.service.Validation;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.chat.ValidationRequest;
import org.springframework.stereotype.Service;


public interface ValidationService {
    String checkValidateForChatroom(int userId, ValidationRequest validationRequest) throws CustomControllerExecption;
}
