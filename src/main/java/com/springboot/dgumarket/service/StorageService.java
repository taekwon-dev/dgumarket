package com.springboot.dgumarket.service;

import com.amazonaws.services.s3.AmazonS3;
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

@Slf4j
@Service
public class StorageService {
    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public boolean chatUploadImages(MultipartFile[] multipartFile, int userId) {

        for (MultipartFile file : multipartFile) {
            log.info("file : {}", file.getOriginalFilename());
            if(file.getOriginalFilename() != null)
                try {
                    // 출처: https://pjh3749.tistory.com/187 [JayTech의 기술 블로그]
                    ObjectMetadata metadata  = new ObjectMetadata();
                    metadata.setContentType(file.getContentType());
                    metadata.setContentLength(file.getSize());
                    metadata.setHeader("filename", file.getOriginalFilename());

                    // s3 multipart upload
                    TransferManager tm = TransferManagerBuilder.standard()
                            .withS3Client(s3Client)
                            .build();
                    String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

                    // TransferManager processes all transfers asynchronously,
                    // so this call returns immediately.
                    Upload upload = tm.upload(bucketName, fileName, file.getInputStream(), metadata);
                    System.out.println("Object upload started : " + file.getOriginalFilename());

                    // Optionally, wait for the upload to finish before continuing.
                    upload.waitForCompletion();
                    System.out.println("Object upload complete : " + file.getOriginalFilename());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
        }
        return true;
    }
}
