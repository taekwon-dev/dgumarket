package com.springboot.dgumarket.controller.awss3;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.service.awss3.AWSS3MultiImgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by TK YOUN (2021-03-06 오후 2:43)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :  AWS S3 두 장 이상의 복수 이미지 API 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/multi-img/")
public class AWSMultipleImgController {

    @Autowired
    private AWSS3MultiImgService awss3MultiImgService;

    @PostMapping("/upload")
    @CheckTargetUserValidate
    public ResponseEntity<ApiResultEntity> uploadMultiImage(
            Authentication authentication
            , @RequestParam(value = "targetId", required = false) Integer targetId
            , @RequestParam("uploadDirPrefix") String uploadDirPrefix
            , @RequestParam(value = "prevFileNames", required = false) String prevFileNames
            , @RequestParam("files") MultipartFile[] multipartFiles) throws CustomControllerExecption {

        // 인증 관련 예외처리
        if (authentication == null) return null;

        // init
        List<String> fileNameLists = null; // AWS 복수 이미지 업로드 반환 값 (파일명 리스트)


        // prevFileName 요소가 없이 요청 받은 경우 {prevFileName = null}
        // prevFileName 값의 여부로 기준이 나뉜다.

        // 0 -> N (새 상품을 업로드 하는 경우 + 상품 관련된 이미지가 있는 경우)
        // 0 -> N (상품 수정 시 0 -> N장으로 수정하는 경우)
        if (prevFileNames == null) {
            fileNameLists = awss3MultiImgService.doUploadImages(multipartFiles, uploadDirPrefix);
        } else {

            // prevFileName -> 공백 제거
            prevFileNames = prevFileNames.replace(" ", "");

            // prevFileName -> `[`, `]` 제거
            prevFileNames = prevFileNames.replace("[", "");
            prevFileNames = prevFileNames.replace("]", "");

            // prevFileName -> `,` 기준으로 리스트 형태로 변환
            // ex) a.jpg,b.jpg,c.jpg --> [a.jpg, b.jpg, c.jpg]
            List<String> prevFileNameList = Arrays.asList(prevFileNames.split(","));

            // 기존에 업로드 됐던 사진의 수
            int prevImageNums = prevFileNameList.size();

            // 새로 업로드하는 이미지 수
            int nextImageNums = multipartFiles.length;

            // 1 N -> N (기존 상품의 정보를 수정하는 경우 + 업로드하는 사진의 수가 동일한 경우 (= 업로드 하는 사진이 다를 수는 있다)
            if (prevImageNums == nextImageNums) {
                // 기존 파일명을 그대로 활용해서 이미지를 업로드
                fileNameLists = awss3MultiImgService.doUploadImgViaPrevNamesAll(multipartFiles, prevFileNameList, uploadDirPrefix);

            } else {
                // 2 N -> N+1 (기존 상품의 정보를 수정하는 경우 + 업로드하는 사진의 수가 더 많은 경우 (= 업로드 하는 사진 중 일부는 다를 수 있다)
                // 기존 파일명의 수 만큼 활용하고, 나머지는 새로 생성해서 업로드한다.
                fileNameLists = awss3MultiImgService.doUploadImgViaPrevNamesPart(multipartFiles, prevFileNameList, uploadDirPrefix);

            }
        }

        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("AWS 복수 이미지 업로드 성공")
                .responseData(fileNameLists.toString()) // 업로드 된 이미지 파일명 리스트 (to String)
                .statusCode(200)
                .build();

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResultEntity> deleteMultiImage(
            @RequestParam("prevFileNames") String prevFileNames,     // 삭제할 이미지 파일명 리스트
            @RequestParam("uploadDirPrefix") String uploadDirPrefix  // 삭제할 이미지의 원본이 저장된 위치 Prefix (리사이즈 이미지 -> AWS Lambda)
            ) {

        // prevFileName -> 공백 제거
        prevFileNames = prevFileNames.replace(" ", "");

        // prevFileName -> `[`, `]` 제거
        prevFileNames = prevFileNames.replace("[", "");
        prevFileNames = prevFileNames.replace("]", "");
        // prevFileName -> `,` 기준으로 리스트 형태로 변환
        // ex) a.jpg,b.jpg,c.jpg --> [a.jpg, b.jpg, c.jpg]
        List<String> prevFileNameList = Arrays.asList(prevFileNames.split(","));

        // (상품) 기존 이미지 N -> 0 (요청하는 경우)
        awss3MultiImgService.doDeleteAllImgViaPrevNamesAll(prevFileNameList, uploadDirPrefix);


        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("AWS 복수 이미지 삭제 성공")
                .responseData(null) // 삭제 이후, (상품) 이미지 저장 경로 값을 null로 지정하기 위해 null 반환
                .statusCode(200)
                .build();

        // 삭제 후 이미지 경로 값을 null로 반환한다.
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }



    @PostMapping("/patch")
    public ResponseEntity<ApiResultEntity> patchMultiImage(
            @RequestParam("prevFileNames") String prevFileNames,     // 기존 이미지 파일명 리스트
            @RequestParam("uploadDirPrefix") String uploadDirPrefix, // 업로드 또는 삭제할 이미지의 원본이 저장된 위치 Prefix (업로드 -> 리사이즈 Lambda, 삭제 -> 삭제 Lambda)
            @RequestParam("files") MultipartFile[] multipartFiles    // 이미지 파일s
    ) {


        // init
        List<String> fileNameLists = null; // AWS 복수 이미지 업로드 반환 값 (파일명 리스트)

        // prevFileName -> 공백 제거
        prevFileNames = prevFileNames.replace(" ", "");

        // prevFileName -> `[`, `]` 제거
        prevFileNames = prevFileNames.replace("[", "");
        prevFileNames = prevFileNames.replace("]", "");
        // prevFileName -> `,` 기준으로 리스트 형태로 변환
        // ex) a.jpg,b.jpg,c.jpg --> [a.jpg, b.jpg, c.jpg]
        List<String> prevFileNameList = Arrays.asList(prevFileNames.split(","));


        // N -> N - M (기존 이미지 수보다 적은 이미지로 수정 후 최종 업로드된 파일 이름 리스트 = 반환 값)
        fileNameLists = awss3MultiImgService.doPatchImgViaPrevNames(multipartFiles, prevFileNameList, uploadDirPrefix);


        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("AWS 복수 이미지 수정 성공 (=복수 업로드 + 삭제 성공)")
                .responseData(fileNameLists.toString()) // 업로드 된 이미지 파일명 리스트 (to String)
                .statusCode(200)
                .build();

        // 삭제 후 이미지 경로 값을 null로 반환한다.
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }


}
