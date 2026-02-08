package com.example.ebank.address.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.example.ebank.address.dto.AddressChangeCommitRequest;
import com.example.ebank.address.dto.AddressChangeResponse;
import com.example.ebank.address.repository.jdbc.AddressRepositoryJdbc;

@Service
public class AddressChangeService {

    private static final Logger log = LogManager.getLogger(AddressChangeService.class);

    private final AddressRepositoryJdbc addressRepositoryJdbc;

    // Javaアプリ側での保存先ディレクトリ（appサーバ内）
    private final Path destBaseDir = Paths.get("/opt/ebank/data/address_proofs/");

    public AddressChangeService(AddressRepositoryJdbc addressRepositoryJdbc) {
        this.addressRepositoryJdbc = addressRepositoryJdbc;
    }

    public AddressChangeResponse commit(Long userId, AddressChangeCommitRequest req) {
        try {
            // 例：PHP から base64 が来ている前提
            byte[] fileBytes = Base64.getDecoder().decode(req.getFileBase64());

            Files.createDirectories(destBaseDir);

            String ext = getExt(req.getFileName());
            String newName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path destPath = destBaseDir.resolve(newName);

            // ローカルファイルにも保存したいなら書き出す
            Files.write(destPath, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // ★ ここを「8引数」に揃える
            addressRepositoryJdbc.insertAddressChangeRequest(
                    userId,
                    req.getPostalCode(),
                    req.getPrefecture(),
                    req.getCity(),
                    req.getAddressLine1(),
                    req.getAddressLine2(),
                    destPath.toString(),  // ログ用にパスも残す（いらなければ null）
                    fileBytes             // DB に入れるバイナリ
            );

            return new AddressChangeResponse("PENDING");

        } catch (Exception e) {
            log.error("Address change failed", e);
            throw new RuntimeException("Address change failed: " + e.getMessage(), e);
        }
    }


    private String getExt(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return (idx == -1) ? "" : filename.substring(idx + 1);
    }
}
