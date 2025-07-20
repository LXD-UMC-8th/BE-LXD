package org.lxdproject.lxd.common.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.common.dto.ImageResponseDto;
import org.lxdproject.lxd.common.entity.Image;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.repository.ImageRepository;
import org.lxdproject.lxd.common.util.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Uploader s3Uploader;
    private final ImageRepository imageRepository;

    public ImageResponseDto uploadImage(MultipartFile file, ImageDir dirName) {
        try {
            String url = s3Uploader.upload(file, dirName.getDirName());
            Image saved = imageRepository.save(
                    Image.builder()
                            .imageUrl(url)
                            .dir(dirName) // enum 자체 저장
                            .build()
            );
            return new ImageResponseDto(url);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }
}

