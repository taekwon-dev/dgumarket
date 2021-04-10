package com.springboot.dgumarket.service.awss3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.aws.AWSImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AWSS3MultiImgService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    // 새로 상품을 업로드 하는 경우 (이미지 첨부)
    // 신고하는 경우 (이미지 첨부)
    // 채팅 사진 업로드 하는 경우 (이미지 첨부)
    public List<String> doUploadImages(MultipartFile[] multipartFiles, String uploadDirPrefix) {

        // init
        String fileType = null; // 각 사진의 파일타입
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로

        List<String> fileNameLists = new ArrayList<>();

        for (int i = 0; i < multipartFiles.length; i++) {

            // 파일명 생성
            // 업로드 디렉토리
            // 파일타입
            fileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);
            fileName = UUID.randomUUID().toString().replace("-", "")+"."+fileType;
            uploadDirOnS3 = uploadDirPrefix+fileName;

            // 반환 할 파일명 리스트를 위해서 파일명 생성 후 리스트에 추가.
            fileNameLists.add(fileName);


            try {
                ObjectMetadata metadata  = new ObjectMetadata();
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


            } catch (IOException e) {
                // DirectoryNotFoundException
                // EndOfStreamException
                // FileNotFoundException
                // FileLoadException
                // PathTooLongException

                // MultipartFile - getInputStream()
                if(uploadDirPrefix.equals("origin/chat")) throw new AWSImageException(errorResponse("IOException, 복수 채팅 이미지 사진 업로드 API", 355, "/api/multi-img/upload"));
                throw new AWSImageException(errorResponse("IOException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            } catch (InterruptedException e) {
                // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                e.printStackTrace();
                if(uploadDirPrefix.equals("origin/chat")) throw new AWSImageException(errorResponse("AmazonServiceException, 복수 채팅 이미지 사진 업로드 API", 355, "/api/multi-img/upload"));
                throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            } catch (AmazonServiceException e) {
                // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                e.printStackTrace();
                if(uploadDirPrefix.equals("origin/chat")) throw new AWSImageException(errorResponse("InterruptedException요, 복수 채팅 이미지 사진 업로드 API", 355, "/api/multi-img/upload"));
                throw new AWSImageException(errorResponse("InterruptedException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));
            }
        }

        return fileNameLists;
    }

    // 상품 수정 시 이미지 변동이 있는 경우
    // N -> N` (새로 수정하는 모든 이미지를 기존 파일명을 활용해서 업로드하는 경우)
    public List<String> doUploadImgViaPrevNamesAll(MultipartFile[] multipartFiles, List<String> prevFileNames, String uploadDirPrefix) {

        // init
        String newFileType = null; // 새로 업로드 하는 이미지 파일 타입
        String prevFileType = null; // 기존 업로드 했던 이미지 파일 타입
        String[] fileTempName = null; // 파일명 스플릿 적용을 위한 스트링 배열 -> 예를 들어 a.jpg --> a (파일타입을 제외한 파일명만 분리해서 활용하기 위해)
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로
        String originFileKey = null; // 기존 파일명과 새로 업로드하는 이미지의 파일 형식이 서로 다른 경우, 기존 파일명 삭제를 위한 오브젝트 키

        List<String> fileNameLists = new ArrayList<>();

        for (int i = 0; i < multipartFiles.length; i++) {

            // 파일명 생성
            // 업로드 디렉토리
            // 파일타입
            newFileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);

            // 새로 업로드한 이미지 파일의 타입이 달라진 경우에 기존 파일명에 포함된 파일형식을 바꿔줘야 한다.
            // example.jpg -> fileTempName[0] : example, fileTempName[1] : jpg
            fileTempName = prevFileNames.get(i).split("\\.");

            fileName = fileTempName[0];
            prevFileType = fileTempName[1];

            // 기존 파일명을 그대로 활용해서 이미지를 업로드 하는 경우 중
            // 기존 이미지 파일 타입과 새로운 이미지 파일 타입이 다른 경우,
            // AWS S3에 동일한 파일명이지만, 서로 다른 파일 타입으로 인해 서로 다른 이미지로 인식되는 문제 (기존 파일명 + 타입에 해당하는 파일 삭제처리)
            // 삭제할 키 값 (=삭제 할 원본의 경로 값)

            if (!prevFileType.equals(newFileType)) {
                // ex) `origin/product/` + `example.jpg`
                originFileKey = uploadDirPrefix+prevFileNames.get(i);

                // AWS S3 Delete API 위한 DeleteObjetctRequest 생성
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);

                try {
                    // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
                    s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
                } catch (AmazonServiceException e) {
                    e.printStackTrace();
                    throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 업로드 API, 파일 타입 문제로 이미지 삭제 API 처리과정에서 예외발생", 352, "/api/multi-img/upload"));
                }
            }


            fileName = fileName+"."+newFileType;
            uploadDirOnS3 = uploadDirPrefix+fileName;

            // 반환 할 파일명 리스트를 위해서 파일명 생성 후 리스트에 추가.
            fileNameLists.add(fileName);

            try {
                ObjectMetadata metadata  = new ObjectMetadata();
                metadata.setContentType(multipartFiles[i].getContentType());
                metadata.setContentLength(multipartFiles[i].getSize());
                metadata.setHeader("filename", prevFileNames.get(0));

                // s3 multipart upload
                TransferManager tm = TransferManagerBuilder.standard()
                        .withS3Client(s3Client)
                        .build();

                // TransferManager processes all transfers asynchronously,
                // so this call returns immediately.
                Upload upload = tm.upload(bucketName, uploadDirOnS3, multipartFiles[i].getInputStream(), metadata);

                // Optionally, wait for the upload to finish before continuing.
                upload.waitForCompletion();

            } catch (IOException e) {
                // DirectoryNotFoundException
                // EndOfStreamException
                // FileNotFoundException
                // FileLoadException
                // PathTooLongException

                // MultipartFile - getInputStream()
                e.printStackTrace();
                throw new AWSImageException(errorResponse("IOException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            } catch (InterruptedException e) {
                // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                e.printStackTrace();
                throw new AWSImageException(errorResponse("InterruptedException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            } catch (AmazonServiceException e) {
                // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                e.printStackTrace();
                throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            }
        }
        return fileNameLists;
    }


    // 상품 수정 시 이미지 변동이 있는 경우
    // N -> N+M (+M에 대해서는 새로운 파일명을 생성해서 업로드를 한다)
    public List<String> doUploadImgViaPrevNamesPart(MultipartFile[] multipartFiles, List<String> prevFileNames, String uploadDirPrefix) {

        // init
        String newFileType = null; // 각 사진의 파일타입
        String prevFileType = null;  // 기존 업로드 했던 이미지 파일 타입
        String[] fileTempName = null; // 파일명 스플릿 적용을 위한 스트링 배열 -> 예를 들어 a.jpg --> a (파일타입을 제외한 파일명만 분리해서 활용하기 위해)
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로
        String originFileKey = null; // 기존 파일명과 새로 업로드하는 이미지의 파일 형식이 서로 다른 경우, 기존 파일명 삭제를 위한 오브젝트 키


        List<String> fileNameLists = new ArrayList<>();

        for (int i = 0; i < multipartFiles.length; i++) {

            ObjectMetadata metadata  = new ObjectMetadata();
            // 업로드한 이미지 파일의 타입
            newFileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);


            if (i > (prevFileNames.size()-1)) {
                // 새로운 파일명을 생성해서 AWS S3에 업로드 해야 하는 경우
                fileName = UUID.randomUUID().toString().replace("-", "")+"."+newFileType;
                uploadDirOnS3 = uploadDirPrefix+fileName;

                // 오브젝트 - 메타데이터
                metadata.setContentType(multipartFiles[i].getContentType());
                metadata.setContentLength(multipartFiles[i].getSize());
                metadata.setHeader("filename", fileName);

            } else {
                // 기존 파일명을 활용해서 클라이언트가 요청한 파일을 AWS S3에 업로드 하는 경우
                fileTempName = prevFileNames.get(i).split("\\.");
                fileName = fileTempName[0];  // 기존 업로드 했던 이미지 파일명
                prevFileType = fileTempName[1]; // 기존 업로드 했던 이미지 파일 타입

                // 기존 파일명을 그대로 활용해서 이미지를 업로드 하는 경우 중
                // 기존 이미지 파일 타입과 새로운 이미지 파일 타입이 다른 경우,
                // AWS S3에 동일한 파일명이지만, 서로 다른 파일 타입으로 인해 서로 다른 이미지로 인식되는 문제 (기존 파일명 + 타입에 해당하는 파일 삭제처리)
                // 삭제할 키 값 (=삭제 할 원본의 경로 값)

                if (!prevFileType.equals(newFileType)) {
                    // ex) `origin/product/` + `example.jpg`
                    originFileKey = uploadDirPrefix+prevFileNames.get(i);

                    // AWS S3 Delete API 위한 DeleteObjetctRequest 생성
                    DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);

                    try {
                        // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
                        s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
                    } catch (AmazonServiceException e) {
                        e.printStackTrace();
                        throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 업로드 API, 파일 타입 문제로 이미지 삭제 API 처리과정에서 예외발생", 352, "/api/multi-img/upload"));
                    }
                }


                fileName = fileName+"."+newFileType;
                uploadDirOnS3 = uploadDirPrefix+fileName;

                // 오브젝트 - 메타데이터
                metadata.setContentType(multipartFiles[i].getContentType());
                metadata.setContentLength(multipartFiles[i].getSize());
                metadata.setHeader("filename", prevFileNames.get(i));
            }

            // 반환 할 파일명 리스트를 위해서 파일명 생성 후 리스트에 추가.
            fileNameLists.add(fileName);

            try {
                // s3 multipart upload
                TransferManager tm = TransferManagerBuilder.standard()
                        .withS3Client(s3Client)
                        .build();

                // TransferManager processes all transfers asynchronously,
                // so this call returns immediately.
                Upload upload = tm.upload(bucketName, uploadDirOnS3, multipartFiles[i].getInputStream(), metadata);

                // Optionally, wait for the upload to finish before continuing.
                upload.waitForCompletion();

            } catch (IOException e) {
                // DirectoryNotFoundException
                // EndOfStreamException
                // FileNotFoundException
                // FileLoadException
                // PathTooLongException

                // MultipartFile - getInputStream()
                e.printStackTrace();
                throw new AWSImageException(errorResponse("IOException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            } catch (InterruptedException e) {
                // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                e.printStackTrace();
                throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            } catch (AmazonServiceException e) {
                // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                e.printStackTrace();
                throw new AWSImageException(errorResponse("InterruptedException, 복수 이미지 사진 업로드 API", 352, "/api/multi-img/upload"));

            }
        }
        return fileNameLists;
    }


    // 상품 수정 시 기존 이미지가 N장 있고,
    // 해당 이미지를 모두 삭제하는 경우 (N -> 0)
    // 복수 이미지 삭제 API만 호출
    public void doDeleteAllImgViaPrevNamesAll(List<String> prevFileNames, String uploadDirPrefix) {

        // init
        String originFileKey = null; // 삭제 할 파일의 원본 경로

        // 삭제 할 파일의 사이즈 (반복문)
        for (int i = 0; i < prevFileNames.size(); i++) {


            // 삭제할 키 값 (=삭제 할 원본의 경로 값)
            // ex) `origin/product/` + `example.jpg`
            originFileKey = uploadDirPrefix+prevFileNames.get(i);

            // AWS S3 Delete API 위한 DeleteObjetctRequest 생성
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);


            try {
                // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
                s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
            } catch (AmazonServiceException e) {
                e.printStackTrace();
                throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 삭제 API", 353, "/api/multi-img/delete"));

            }

        }
    }

    // N -> N-M (N > M, M > 0)
    // ex) 5 -> 4, 5 -> 3, 3 -> 1 ...
    // 기존 파일명을 활용하지만, 상품 수정 후 업로드하는 이미지가 더 적은 경우
    // 따라서, 기존 파일명을 활용해서 업로드 하는 부분과 기존 파일명으로 S3에 저장되어 있는 파일을 삭제하는 API가 같이 처리됌.
    // 반환 값은 해당 상품에 해당하는 이미지 경로
    public List<String> doPatchImgViaPrevNames(MultipartFile[] multipartFiles, List<String> prevFileNames, String uploadDirPrefix) {

        // 반복문 -> 기준 : 기존 업로드 됐던 파일의 수(N)
        // (새로 업로드 한 파일 수  - 1) 보다 큰 경우 -> 복수 이미지 삭제 처리

        // ex) 기존 (A, B, C, D, E) 새로 업로드한 이미지 (A`, B`)
        //  5 -> 2 이므로, C, D, E에 해당하는 파일들은 AWS S3에 삭제 요청을 통해 삭제를 해야 한다.
        // 기존 파일 수를 기준으로 반복문 로직을 활용하므로, 새로 업로드 한 파일의 수부터는 삭제 요청을 보내고,
        // 그 이전은 업로드 요청을 보내면 된다. 이 때 기존 파일명을 활용해서 업로드 한다.

        // init
        String newFileType = null; // 새로 업로드 하는 이미지 파일 타입
        String prevFileType = null; // 기존 업로드 했던 이미지 파일 타입
        String[] fileTempName = null; // 파일명 스플릿 적용을 위한 스트링 배열 -> 예를 들어 a.jpg --> a (파일타입을 제외한 파일명만 분리해서 활용하기 위해)
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로

        // 1. 5->3 에서 기존 이미지 중 삭제 처리를 해야하는 이미지 오브젝트 키
        // 2. 기존 파일명과 새로 업로드하는 이미지의 파일 형식이 서로 다른 경우, 기존 파일명 삭제를 위한 오브젝트 키
        String originFileKey = null;




        List<String> fileNameLists = new ArrayList<>();


        for (int i = 0; i < prevFileNames.size(); i++) {

            if (i >= multipartFiles.length) {
                // 삭제 요청
                // 삭제할 키 값 (=삭제 할 원본의 경로 값)
                // ex) `origin/product/` + `example.jpg`
                originFileKey = uploadDirPrefix+prevFileNames.get(i);

                // AWS S3 Delete API 위한 DeleteObjetctRequest 생성
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);
                try {
                    // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
                    s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
                } catch (AmazonServiceException e) {
                    e.printStackTrace();
                    throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 수정 API, 기존 파일명 수보다 큰 인덱스 사진의 원본 삭제 과정", 354, "/api/multi-img/patch"));

                }


            } else {
                // 업로드한 이미지 파일의 타입
                newFileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);
                // 기존 파일명을 활용해서 클라이언트가 요청한 파일을 AWS S3에 업로드 하는 경우
                fileTempName = prevFileNames.get(i).split("\\.");

                fileName = fileTempName[0]; // 기존 업로드 했던 이미지 파일
                prevFileType = fileTempName[1]; // 기존 업로드 했던 이미지 파일 타입 명

                // 기존 파일명을 그대로 활용해서 이미지를 업로드 하는 경우 중
                // 기존 이미지 파일 타입과 새로운 이미지 파일 타입이 다른 경우,
                // AWS S3에 동일한 파일명이지만, 서로 다른 파일 타입으로 인해 서로 다른 이미지로 인식되는 문제 (기존 파일명 + 타입에 해당하는 파일 삭제처리)
                // 삭제할 키 값 (=삭제 할 원본의 경로 값)

                if (!prevFileType.equals(newFileType)) {
                    // ex) `origin/product/` + `example.jpg`
                    originFileKey = uploadDirPrefix+prevFileNames.get(i);

                    // AWS S3 Delete API 위한 DeleteObjetctRequest 생성
                    DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);

                    try {
                        // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
                        s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
                    } catch (AmazonServiceException e) {
                        e.printStackTrace();
                        throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 수 API, 파일 타입 문제로 이미지 삭제 API 처리과정에서 예외발생", 354, "/api/multi-img/upload"));

                    }
                }


                fileName = fileName+"."+newFileType;
                uploadDirOnS3 = uploadDirPrefix+fileName;

                // 오브젝트 - 메타데이터
                ObjectMetadata metadata  = new ObjectMetadata();
                metadata.setContentType(multipartFiles[i].getContentType());
                metadata.setContentLength(multipartFiles[i].getSize());
                metadata.setHeader("filename", prevFileNames.get(i));

                // 반환 할 파일명 리스트를 위해서 파일명 생성 후 리스트에 추가.
                fileNameLists.add(fileName);

                try {
                    // s3 multipart upload
                    TransferManager tm = TransferManagerBuilder.standard()
                            .withS3Client(s3Client)
                            .build();

                    // TransferManager processes all transfers asynchronously,
                    // so this call returns immediately.
                    Upload upload = tm.upload(bucketName, uploadDirOnS3, multipartFiles[i].getInputStream(), metadata);

                    // Optionally, wait for the upload to finish before continuing.
                    upload.waitForCompletion();

                } catch (IOException e) {
                    // DirectoryNotFoundException
                    // EndOfStreamException
                    // FileNotFoundException
                    // FileLoadException
                    // PathTooLongException

                    // MultipartFile - getInputStream()
                    e.printStackTrace();
                    throw new AWSImageException(errorResponse("IOException, 복수 이미지 사진 수정 API", 354, "/api/multi-img/patch"));

                } catch (InterruptedException e) {
                    // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                    e.printStackTrace();
                    throw new AWSImageException(errorResponse("AmazonServiceException, 복수 이미지 사진 수정 API", 354, "/api/multi-img/patch"));

                } catch (AmazonServiceException e) {
                    // void waitForCompletion() throws AmazonClientException, AmazonServiceException, InterruptedException;
                    e.printStackTrace();
                    throw new AWSImageException(errorResponse("InterruptedException, 복수 이미지 사진 수정 API", 354, "/api/multi-img/patch"));

                }

            }

        }
        return fileNameLists;
    }

    public String errorResponse(String errMsg, int resultCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int statusCode;
        //     Date timestamp;
        //     String message;
        //     String requestPath;
        //     String pathToMove;
        // }

        // errorCode에 따라서 예외 결과 클라이언트가 특정 페이지로 요청해야 하는 경우가 있다.
        // 그 경우 pathToMove 항목을 채운다.

        // init
        ErrorMessage errorMessage = null;

        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;

        // 예외 처리 결과 클라이언트가 이동시킬 페이지 참조 값을 반환해야 하는 경우 에러 코드 범위
        // 예외처리 결과 클라이언트가 __페이지를 요청해야 하는 경우
        // 해당 페이지 정보 포함해서 에러 메시지 반환
        if (resultCode >= 300 && resultCode < 350) {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .pathToMove("/shop/main/index") // 추후 index 페이지 경로 바뀌면 해당 경로 값으로 수정 할 것.
                    .build();
        } else {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .build();

        }

        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }






}
