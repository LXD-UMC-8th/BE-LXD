package org.lxdproject.lxd.global.common.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.common.dto.ImageDTO;
import org.lxdproject.lxd.global.common.entity.enums.ImageDir;
import org.lxdproject.lxd.global.s3.S3FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3FileService s3FileService;

    public ImageDTO uploadImage(MultipartFile file, ImageDir dirName) {
        String url = s3FileService.uploadImage(file, dirName.getDirName());
        return new ImageDTO(url);
    }
}

