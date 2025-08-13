package org.lxdproject.lxd.common.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.common.dto.ImageResponseDTO;
import org.lxdproject.lxd.common.entity.Image;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.repository.ImageRepository;
import org.lxdproject.lxd.infra.storage.S3FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3FileService s3FileService;
    private final ImageRepository imageRepository;

    public ImageResponseDTO uploadImage(MultipartFile file, ImageDir dirName) {
        try {
            String url = s3FileService.upload(file, dirName.getDirName());
            Image saved = imageRepository.save(
                    Image.builder()
                            .imageUrl(url)
                            .dir(dirName) // enum 자체 저장
                            .build()
            );
            return new ImageResponseDTO(url);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }
}

