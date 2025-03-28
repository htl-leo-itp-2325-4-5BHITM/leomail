package at.htlleonding.leomail.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@ApplicationScoped
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BIT = 128;

    @ConfigProperty(name = "encryption.key")
    public String encryptionKey;

    /**
     * Encrypts the given value using AES-GCM with a random IV.
     *
     * @param value The value to encrypt.
     * @return The encrypted value, with the IV prepended and then Base64-encoded.
     */
    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec secretKey = getSecretKey(); // Derive the AES key from the passphrase

            byte[] iv = generateIV(); // Generate a random IV
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIV = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIV, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, encryptedWithIV, IV_SIZE, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIV); // Encode to Base64
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting", e);
        }
    }

    /**
     * Decrypts the given encrypted value (with IV prepended).
     *
     * @param encryptedValue The encrypted value to decrypt.
     * @return The decrypted value.
     */
    public String decrypt(String encryptedValue) {
        try {
            byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(decodedValue, 0, iv, 0, IV_SIZE);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            SecretKeySpec secretKey = getSecretKey();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decrypted = cipher.doFinal(decodedValue, IV_SIZE, decodedValue.length - IV_SIZE);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting", e);
        }
    }

    /**
     * Derives the AES key from the provided UTF-8 passphrase using PBKDF2.
     * This allows the passphrase to contain any UTF-8 characters.
     *
     * @return A SecretKeySpec object containing the derived AES key.
     */
    private SecretKeySpec getSecretKey() {
        try {
            // Use a fixed salt for demonstration. For production use a securely generated salt stored securely.
            byte[] salt = "fixedSaltValue".getBytes(StandardCharsets.UTF_8);
            int iterations = 65536;
            int keyLength = 256; // in bits

            KeySpec spec = new PBEKeySpec(encryptionKey.trim().toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] keyBytes = factory.generateSecret(spec).getEncoded();

            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating secret key", e);
        }
    }

    /**
     * Generates a random IV for AES-GCM encryption.
     *
     * @return A byte array containing the IV.
     */
    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}