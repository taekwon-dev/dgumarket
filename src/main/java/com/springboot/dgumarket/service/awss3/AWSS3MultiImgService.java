package com.springboot.dgumarket.service.awss3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return fileNameLists;
    }

    // 상품 수정 시 이미지 변동이 있는 경우
    // N -> N` (새로 수정하는 모든 이미지를 기존 파일명을 활용해서 업로드하는 경우)
    public List<String> doUploadImgViaPrevNamesAll(MultipartFile[] multipartFiles, List<String> prevFileNames, String uploadDirPrefix) {

        // init
        String fileType = null; // 각 사진의 파일타입
        String[] fileTempName = null; // 파일명 스플릿 적용을 위한 스트링 배열 -> 예를 들어 a.jpg --> a (파일타입을 제외한 파일명만 분리해서 활용하기 위해)
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로

        List<String> fileNameLists = new ArrayList<>();

        for (int i = 0; i < multipartFiles.length; i++) {

            // 파일명 생성
            // 업로드 디렉토리
            // 파일타입
            fileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);

            // 새로 업로드한 이미지 파일의 타입이 달라진 경우에 기존 파일명에 포함된 파일형식을 바꿔줘야 한다.
            fileTempName = prevFileNames.get(i).split("\\.");
            fileName = fileTempName[0];
            fileName = fileName+"."+fileType;
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

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return fileNameLists;
    }


    // 상품 수정 시 이미지 변동이 있는 경우
    // N -> N+M (+M에 대해서는 새로운 파일명을 생성해서 업로드를 한다)
    public List<String> doUploadImgViaPrevNamesPart(MultipartFile[] multipartFiles, List<String> prevFileNames, String uploadDirPrefix) {

        // init
        String fileType = null; // 각 사진의 파일타입
        String[] fileTempName = null; // 파일명 스플릿 적용을 위한 스트링 배열 -> 예를 들어 a.jpg --> a (파일타입을 제외한 파일명만 분리해서 활용하기 위해)
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로

        List<String> fileNameLists = new ArrayList<>();

        for (int i = 0; i < multipartFiles.length; i++) {

            ObjectMetadata metadata  = new ObjectMetadata();
            // 업로드한 이미지 파일의 타입
            fileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);


            if (i > (prevFileNames.size()-1)) {
                // 새로운 파일명을 생성해서 AWS S3에 업로드 해야 하는 경우
                fileName = UUID.randomUUID().toString().replace("-", "")+"."+fileType;
                uploadDirOnS3 = uploadDirPrefix+fileName;

                // 오브젝트 - 메타데이터
                metadata.setContentType(multipartFiles[i].getContentType());
                metadata.setContentLength(multipartFiles[i].getSize());
                metadata.setHeader("filename", fileName);

            } else {
                // 기존 파일명을 활용해서 클라이언트가 요청한 파일을 AWS S3에 업로드 하는 경우
                fileTempName = prevFileNames.get(i).split("\\.");
                fileName = fileTempName[0];
                fileName = fileName+"."+fileType;
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

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                log.error("복수 이미지 삭제 요청 중 에러 / 에러 메시지 : " + e.getErrorMessage());
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
        String fileType = null; // 각 사진의 파일타입
        String[] fileTempName = null; // 파일명 스플릿 적용을 위한 스트링 배열 -> 예를 들어 a.jpg --> a (파일타입을 제외한 파일명만 분리해서 활용하기 위해)
        String fileName = null; // 각 사진의 파일명
        String uploadDirOnS3 = null; // AWS S3 업로드 경로


        List<String> fileNameLists = new ArrayList<>();


        for (int i = 0; i < prevFileNames.size(); i++) {

            if (i >= multipartFiles.length) {
                // 삭제 요청
                String originFileKey = null;  // 삭제 할 파일의 저장 경로

                // 삭제할 키 값 (=삭제 할 원본의 경로 값)
                // ex) `origin/product/` + `example.jpg`
                originFileKey = uploadDirPrefix+prevFileNames.get(i);

                // AWS S3 Delete API 위한 DeleteObjetctRequest 생성
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, originFileKey);
                try {
                    // 삭제 할 대상이 AWS S3에 없어도 예외가 발생하지 않는다.
                    s3Client.deleteObject(deleteObjectRequest); // Could throw SdkClientException, AmazonServiceException.
                } catch (AmazonServiceException e) {
                    log.error("복수 이미지 삭제 요청 중 에러 / 에러 메시지 : " + e.getErrorMessage());
                }


            } else {
                // 업로드한 이미지 파일의 타입
                fileType = multipartFiles[i].getOriginalFilename().substring(multipartFiles[i].getOriginalFilename().lastIndexOf(".") + 1);
                // 기존 파일명을 활용해서 클라이언트가 요청한 파일을 AWS S3에 업로드 하는 경우
                fileTempName = prevFileNames.get(i).split("\\.");

                fileName = fileTempName[0];
                fileName = fileName+"."+fileType;
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

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
        return fileNameLists;
    }
}
