package org.lxdproject.lxd.global.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.StorageHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadImage(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(file.getContentType())
                .build();

        try (InputStream is = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, file.getSize()));
        } catch (IOException e) {
            throw new StorageHandler(ErrorStatus.FILE_STREAM_READ_FAILED);
        } catch (S3Exception e) {
            throw new StorageHandler(ErrorStatus.S3_UPLOAD_FAILED);
        } catch (SdkException e) {
            throw new StorageHandler(ErrorStatus.AWS_SDK_CLIENT_ERROR);
        }

        return getFileUrl(fileName);
    }

    private String getFileUrl(String fileName) {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }

    public List<String> extractS3KeysFromUrls(List<String> imageUrls) {
        return imageUrls.stream()
                .map(this::extractKeyFromUrl)
                .filter(Objects::nonNull)
                .toList();
    }

    private String extractKeyFromUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getPath().substring(1); // /diary/abc.jpg → diary/abc.jpg
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void deleteImages(List<String> keys) {
        if (keys.isEmpty()) return;

        List<ObjectIdentifier> objectsToDelete = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        Delete delete = Delete.builder()
                .objects(objectsToDelete)
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(request);
            if (!response.hasErrors()) {
                log.info("모든 S3 이미지 삭제 성공");
            } else {
                log.warn("일부 S3 이미지 삭제 실패: {}", response.errors());
            }
        } catch (S3Exception e) {
            log.warn("S3 이미지 삭제 중 예외 발생: {}", e.awsErrorDetails().errorMessage());
        }
    }

    public void deleteImage(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        if (key == null || key.isBlank()) {
            log.warn("유효하지 않은 S3 이미지 URL입니다: {}", imageUrl);
            return;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(request);
            log.info("S3 이미지 삭제 성공: {}", key);
        } catch (S3Exception e) {
            log.warn("S3 이미지 삭제 실패: {} - {}", key, e.awsErrorDetails().errorMessage());
        }
    }


}

