package org.example.Model.Kyc;

import java.time.LocalDateTime;

public class KycFile {
    private int id;
    private int kycId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private byte[] fileData;
    private LocalDateTime updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKycId() {
        return kycId;
    }

    public void setKycId(int kycId) {
        this.kycId = kycId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS kyc_files (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    kyc_id INT NOT NULL,
                    file_name VARCHAR(255) NOT NULL,
                    file_path VARCHAR(255) NULL,
                    file_type VARCHAR(20) NOT NULL,
                    file_size BIGINT NOT NULL,
                    file_data LONGBLOB NOT NULL,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY ux_kyc_file_name (kyc_id, file_name),
                    FOREIGN KEY (kyc_id) REFERENCES kyc(id) ON DELETE CASCADE
                );
                """;
    }
}