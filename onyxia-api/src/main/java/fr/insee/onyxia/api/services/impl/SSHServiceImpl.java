package fr.insee.onyxia.api.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.stream.Collectors;

import fr.insee.onyxia.api.dao.StorageS3;
import fr.insee.onyxia.api.dao.StorageS3Client;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.examples.ByteArrayHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.insee.onyxia.api.services.SSHService;
import fr.insee.onyxia.api.services.UserDataService;
import fr.insee.onyxia.api.services.ssh.PaireClefs;
import fr.insee.onyxia.model.User;

@Service
public class SSHServiceImpl implements SSHService {
   
   @Autowired
   private UserDataService userDataService;
   
   @Autowired
   private StorageS3Client ss3Client;

   @Value("${s3.accesskey}")
   private String MINIO_ACCESSKEY;

   @Value("${s3.secretkey}")
   private String MINIO_SECRETKEY;

   @Value("${s3.region}")
   private String MINIO_REGION;

   @Value("${s3.url.alpha}")
   private String MINIO_URL;

      public void updateSsh(User user)
            throws Exception {
         String bucket = "onyxia";
         PaireClefs paireClefs = generateKeys(user.getEmail());

         int length = 16;
         String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789";
         String password = new SecureRandom()
               .ints(length, 0, chars.length())
               .mapToObj(i -> "" + chars.charAt(i))
               .collect(Collectors.joining());

         user.setPassword(password);
         user.setSshPublicKey(paireClefs.getPublicKey());
         
         String encryptKey = encrypt(paireClefs.getPrivateKey(), password);

         ss3Client.uploadObject(MINIO_REGION, MINIO_URL, MINIO_ACCESSKEY, MINIO_SECRETKEY, null, bucket, user.getIdep() + "_rsa.gpg", new ByteArrayInputStream(encryptKey.getBytes("UTF-8")), encryptKey.getBytes("UTF-8").length);
         userDataService.saveUserData(user);
      }
      
      private static PaireClefs generateKeys(String user)
            throws IOException, NoSuchAlgorithmException {
         
         KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
         keyGen.initialize(2048);
         KeyPair keyPair = keyGen.genKeyPair();
         
         return PaireClefs.newInstance()
            .setPublicKey(encodePublicKey(keyPair.getPublic(), user))
            .setPrivateKey(
                  "-----BEGIN RSA PRIVATE KEY-----\n"
                  + encodePrivateKey(keyPair.getPrivate())
                  + "\n-----END RSA PRIVATE KEY-----\n"
                  )
            .build();
      }
      
      
      private static String encrypt(String uncrypted, String password)
            throws IOException, PGPException, NoSuchProviderException {
         Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

         byte[] encryptedByteArray = ByteArrayHandler.encrypt(
               uncrypted.getBytes(),
               password.toCharArray(),
               "id_rsa",
               SymmetricKeyAlgorithmTags.AES_256,
               true
               );
         String encryptedString = new String(encryptedByteArray);

         return encryptedString;

      }
      
      
      private static String encodePrivateKey(PrivateKey privateKey) throws IOException {
         String privateKeyEncoded;
         
         RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
         privateKeyEncoded = new String(Base64.getEncoder().encode(rsaPrivateKey.getEncoded()));
         
         return privateKeyEncoded;
      }
      
      
      private static String encodePublicKey(PublicKey publicKey, String user) throws IOException {
         String publicKeyEncoded;
         
         RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
         ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(byteOs);
         dos.writeInt("ssh-rsa".getBytes().length);
         dos.write("ssh-rsa".getBytes());
         dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
         dos.write(rsaPublicKey.getPublicExponent().toByteArray());
         dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
         dos.write(rsaPublicKey.getModulus().toByteArray());
         publicKeyEncoded = new String(Base64.getEncoder().encode(byteOs.toByteArray()));
         return "ssh-rsa " + publicKeyEncoded + " " + user;
      }
}
