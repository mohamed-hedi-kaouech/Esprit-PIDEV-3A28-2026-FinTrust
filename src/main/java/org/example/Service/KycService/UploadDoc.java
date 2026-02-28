package org.example.Service.KycService;

public class UploadDoc {
    private final String fileName;
    private final String mimeType;
    private final long size;
    private final byte[] data;

    public UploadDoc(String fileName, String mimeType, long size, byte[] data) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.size = size;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }
}