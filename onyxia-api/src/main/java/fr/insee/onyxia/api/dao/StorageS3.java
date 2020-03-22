package fr.insee.onyxia.api.dao;

import java.io.InputStream;

public interface StorageS3 {

    public void uploadObject(String region, String url, String accessKey, String secretKey, String accessToken, String bucket, String filename, InputStream inputStream, long contentLength);

}
