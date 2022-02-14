package fr.insee.onyxia.api.controller.api.s3;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;

@Tag(name = "S3", description = "S3 related services")
@RequestMapping(value={"/api/s3", "/s3"})
@RestController
@SecurityRequirement(name = "auth")
public class S3Controller {


    @PostMapping
    public void createBucket(String awsRegion, String accessKey, String secretKey, String sessionToken, String bucketName) {
        software.amazon.awssdk.regions.Region region = software.amazon.awssdk.regions.Region.of(awsRegion);
        AwsCredentials creds = AwsSessionCredentials.create(accessKey,secretKey,sessionToken);
        try (S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build()) {
            // Create bucket
            CreateBucketRequest req = CreateBucketRequest.builder().bucket(bucketName).build();
            SdkHttpResponse response = s3.createBucket(req).sdkHttpResponse();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Bucket creation failed "+response.statusText());
            }
            // Apply CORS
            List<String> allowedMethods = new ArrayList<>();
            allowedMethods.add("GET");
            allowedMethods.add("POST");
            allowedMethods.add("PUT");
            allowedMethods.add("DELETE");
            allowedMethods.add("HEAD");
            CORSRule rule = CORSRule.builder().allowedHeaders("*").allowedMethods(allowedMethods).allowedOrigins("*").build();
            CORSConfiguration corsConfig = CORSConfiguration.builder().corsRules(rule).build();
            SdkHttpResponse corsResponse = s3.putBucketCors(PutBucketCorsRequest.builder().bucket(bucketName).corsConfiguration(corsConfig).build()).sdkHttpResponse();
            if (!corsResponse.isSuccessful()) {
                throw new RuntimeException("Bucket creation failed "+response.statusText());
            }
        }
    }
}
