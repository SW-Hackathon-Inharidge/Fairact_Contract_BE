package org.inharidge.fairact_contract_be.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class FileHashUtil {
    public static String getSha1FromMultipartFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.sha1Hex(inputStream);
        }
    }
}
