package org.inharidge.fairact_contract_be.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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

    public void deleteFile(String fileUri) {
        try {
            String decodedUri = URLDecoder.decode(fileUri, StandardCharsets.UTF_8);
            String prefix = url.endsWith("/") ? url : url + "/";

            if (!decodedUri.startsWith(prefix)) {
                throw new IllegalArgumentException("MinIO URI가 잘못되었습니다.");
            }

            // contracts/1720430812150_test.pdf
            String path = decodedUri.substring(prefix.length());

            // bucket: contracts, object: 1720430812150_test.pdf
            String[] parts = path.split("/", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("MinIO URI 형식이 잘못되었습니다.");
            }

            String bucketName = parts[0];
            String objectName = parts[1];

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("MinIO 파일 삭제 실패", e);
        }
    }

}
