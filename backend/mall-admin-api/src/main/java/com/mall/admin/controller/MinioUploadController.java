package com.mall.admin.controller;

import com.mall.common.api.CommonResult;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * MinIO 文件上传 — 对标 V1 OssController
 */
@Tag(name = "MinIOUpload", description = "文件上传")
@RestController
@RequestMapping("/minio")
public class MinioUploadController {

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    private final MinioClient minioClient;

    public MinioUploadController(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public CommonResult<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            String url = endpoint + "/" + bucketName + "/" + objectName;
            return CommonResult.success(Map.of("url", url, "name", objectName));
        } catch (Exception e) {
            return CommonResult.failed("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文件删除")
    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestParam("objectName") String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return CommonResult.success(null);
        } catch (Exception e) {
            return CommonResult.failed("删除失败: " + e.getMessage());
        }
    }
}
