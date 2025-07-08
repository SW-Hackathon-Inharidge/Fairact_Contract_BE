package org.inharidge.fairact_contract_be.util;

import org.springframework.web.multipart.MultipartFile;

public class FileNameUtil {

    public static String extractFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) return null;

        // 슬래시나 백슬래시를 기준으로 가장 마지막 요소를 반환 (윈도우/리눅스 모두 대응)
        return originalFilename.substring(originalFilename.lastIndexOf('/') + 1)
                .substring(originalFilename.lastIndexOf('\\') + 1);
    }
}
