package services;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class ParameterService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<String, String> scDictionary = new HashMap<>();
    private static final String ENCRYPT_KEY = "KeyGeneratedByJesusLNV";

    private ParameterService() {
    }

    /**
     * @param parameterName  Is the name of the parameter to be stored
     * @param parameterValue Is the value of the parameter to be stored
     */
    public static void setParameter(String parameterName, String parameterValue) {
        scDictionary.put(parameterName, parameterValue);
    }

    /**
     * @param parameterName Is the name of the parameter to be retrieved
     * @return Returns the value of the parameter
     */
    public static String getParameter(String parameterName) {
        return scDictionary.get(parameterName);
    }

    /**
     * @param stringToEncrypt Is the name of the String to be encrypted
     * @return Returns the String encrypted
     */
    public static String encryptString(String stringToEncrypt) {
        String stringEncrypted = null;
        try {
            SecretKeySpec secretKeySpec = createCustomSecretKeySpec();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));
            stringEncrypted = Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            LOGGER.error("Error while encrypting: {}", ex.getMessage());
        }
        return stringEncrypted;
    }

    /**
     * @param stringToDecrypt Is the name of the String to be decrypted
     * @return Returns the String decrypted
     */
    public static String decryptString(String stringToDecrypt) {
        String stringDecrypted = null;
        try {
            SecretKeySpec secretKeySpec = createCustomSecretKeySpec();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decrypted = Base64.getDecoder().decode(stringToDecrypt);
            stringDecrypted = new String(cipher.doFinal(decrypted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            LOGGER.error("Error while decrypting: {}", ex.getMessage());
        }
        return stringDecrypted;
    }

    private static SecretKeySpec createCustomSecretKeySpec() {
        SecretKeySpec secretKeySpec = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] tmpKey = messageDigest.digest(ENCRYPT_KEY.getBytes(StandardCharsets.UTF_8));
            tmpKey = Arrays.copyOf(tmpKey, 16);
            secretKeySpec = new SecretKeySpec(tmpKey, "AES");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Error creating Custom Key Spec: {}", ex.getMessage());
        }
        return secretKeySpec;
    }

    /**
     * @param url     Is the URL from the API to be tested
     * @param headers Are the Headers from the API to be tested
     * @return Returns the response from requested Service
     */
    public static HttpResponse requestGetService(String url, Map<String, String> headers) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpGet.addHeader(header.getKey(), header.getValue());
        }
        try {
            LOGGER.info("Starting HttpGet Service Test for API: " + url);
            return httpClient.execute(httpGet);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * @param url              Is the URL from the API to be tested
     * @param headers          Are the Headers from the API to be tested
     * @param bodyFileLocation Is the Body File location to use
     * @return Returns the response from requested Service
     */
    public static HttpResponse requestPostService(String url, Map<String, String> headers, String bodyFileLocation) {
        //Start HTTP Parameters
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpPost.addHeader(header.getKey(), header.getValue());
        }
        try {
            String body = new String(Files.readAllBytes(Paths.get(bodyFileLocation)));
            StringEntity stringEntity = new StringEntity(body);
            httpPost.setEntity(stringEntity);
            LOGGER.info("Starting HttpPost Service Test for API: " + url);
            return httpClient.execute(httpPost);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * @param url              Is the URL from the API to be tested
     * @param headers          Are the Headers from the API to be tested
     * @param bodyFileLocation Is the Body File location to use
     * @return Returns the response from requested Service
     */
    public static HttpResponse requestPutService(String url, Map<String, String> headers, String bodyFileLocation) {
        //Start HTTP Parameters
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpPut.addHeader(header.getKey(), header.getValue());
        }
        try {
            String body = new String(Files.readAllBytes(Paths.get(bodyFileLocation)));
            StringEntity stringEntity = new StringEntity(body);
            httpPut.setEntity(stringEntity);
            LOGGER.info("Starting HttpPut Service Test for API: " + url);
            return httpClient.execute(httpPut);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }
}