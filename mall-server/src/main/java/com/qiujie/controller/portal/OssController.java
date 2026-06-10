package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController("portalOssController")
@RequestMapping("/portal/oss")
@Tag(name = "门户端-文件管理")
public class OssController {

    private final OssService ossService;

    public OssController(OssService ossService) {
        this.ossService = ossService;
    }

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public ResponseDTO<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(value = "dir", defaultValue = "common") String dir) throws IOException {
        if (file.isEmpty()) return Response.error(BusinessStatusEnum.FILE_EMPTY);
        String fileName = ossService.upload(file, dir);
        String url = ossService.getUrl(fileName);
        return Response.success(Map.of("fileName", fileName, "url", url));
    }
}
