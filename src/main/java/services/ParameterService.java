package services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
}