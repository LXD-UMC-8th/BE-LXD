package org.lxdproject.lxd.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

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

    public void deleteFiles(List<String> keys) {
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

}

