package fr.insee.onyxia.api.dao;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class StorageS3Client implements StorageS3 {

    public void uploadObject(String region, String url, String accessKey, String secretKey, String accessToken, String bucket, String filename, InputStream inputStream, long contentLength) {
        // TODO: Vérification que le dossier existe
        System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
        AwsClientBuilder.EndpointConfiguration conf = new AwsClientBuilder.EndpointConfiguration(url + "/" + bucket, region);
        AWSCredentials creds = (accessToken == null ? new BasicAWSCredentials(accessKey, secretKey) : new BasicSessionCredentials(accessKey, secretKey, accessToken));
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withEndpointConfiguration(conf)
                .build();
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(contentLength);
        s3Client.putObject(
                "",
                filename,
                inputStream,
                meta);
    }

}
