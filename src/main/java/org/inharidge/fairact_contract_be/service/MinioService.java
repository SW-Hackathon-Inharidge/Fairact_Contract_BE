package org.inharidge.fairact_contract_be.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {
    @Value("${minio.url}")
    private String url;

    @Value("${minio.bucket}")
    private String bucket;

    private final MinioClient minioClient;

    public String uploadFile(MultipartFile file) {
        try {
            // 원래 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 랜덤 UUID로 파일 이름 생성 + 확장자 유지
            String objectName = UUID.randomUUID().toString() + extension;

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

            // 반환할 URL
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

    public String getPreSignedUrlByBucketUrl(String bucketUrl) {
        try {
            // publicUrl 기준으로 앞부분 제거
            if (!bucketUrl.startsWith(url)) {
                throw new IllegalArgumentException("Invalid bucketUrl: " + bucketUrl);
            }

            String path = bucketUrl.substring(url.length() + 1); // remove base url and trailing slash
            String[] parts = path.split("/", 2); // split into bucket and object

            if (parts.length != 2) {
                throw new IllegalArgumentException("bucketUrl must be in the format: " + url + "/<bucket>/<object>");
            }

            String bucketName = parts[0];
            String objectName = parts[1];

            // Pre-signed GET URL 생성
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build());

        } catch (Exception e) {
            throw new RuntimeException("Pre-signed URL 생성 실패: " + bucketUrl, e);
        }
    }

    public String parsePresignedUrlToStorageUrl(String presignedUrl) {
        try {
            URI uri = new URI(presignedUrl);
            String[] segments = uri.getPath().split("/", 3); // ["", "bucket", "object"]

            if (segments.length != 3 || segments[1].isEmpty() || segments[2].isEmpty()) {
                throw new IllegalArgumentException("Invalid MinIO PreSigned URL format: " + uri.getPath());
            }

            String bucket = URLDecoder.decode(segments[1], StandardCharsets.UTF_8);
            String fullObjectName = URLDecoder.decode(segments[2], StandardCharsets.UTF_8);

//            // "_"로 나눈 뒤 뒤쪽만 취함 (timestamp 제거)
//            String[] objectParts = fullObjectName.split("_", 2);
//            String objectName = objectParts.length == 2 ? objectParts[1] : fullObjectName;

            return url + "/" + bucket + "/" + fullObjectName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MinIO PreSigned URL: " + presignedUrl, e);
        }
    }
}