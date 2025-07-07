package org.inharidge.fairact_contract_be.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.url}")
    private String url;

    @Value("${minio.bucket}")
    private String bucket;

    private final MinioClient minioClient;

    public String uploadFile(MultipartFile file) {
        try {

            String objectName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // 버킷이 없으면 생성
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket).build());
            }

            // 업로드
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 반환할 URL 또는 경로
            return url + "/" + bucket + "/" + objectName;

        } catch (Exception e) {
            throw new RuntimeException("Minio 업로드 실패", e);
        }
    }
}
