package iljin.framework.core.security;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import javax.crypto.Cipher;

import lombok.SneakyThrows;


public class CipherTest {

    private KeyPairGenerator generator;
    private KeyFactory keyFactory;
    private KeyPair keypair;
    private Cipher cipher;

    // 1024비트 RSA 키쌍을 생성
    public CipherTest() {
        try{
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            keyFactory = KeyFactory.getInstance("RSA");
            cipher = Cipher.getInstance("RSA");
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public HashMap<String, Object> createRSA() {
        HashMap<String, Object> rsa = new HashMap<String, Object>();
        try{
            keypair = generator.generateKeyPair();
            PublicKey publicKey = keypair.getPublic();
            PrivateKey privateKey = keypair.getPrivate();

            RSAPublicKeySpec publicSpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
            String modulus = publicSpec.getModulus().toString(16);
            String exponent = publicSpec.getPublicExponent().toString(16);
            rsa.put("privateKey", privateKey);
            rsa.put("modulus", modulus);
            rsa.put("exponent", exponent);

        }catch (Exception e){
        }

        return rsa;
    }

    // Key로 RSA 복호화를 수행
    public String getDecryptText(PrivateKey privateKey, String ecryptText) throws Exception {
        cipher.init(cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(hexToByteArray(ecryptText));

        return new String(decryptedBytes, "UTF-8");
    }

    // Key로 RSA 암호화를 수행
    public String setEncryptText(PublicKey publicKey, String encryptText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(encryptText.getBytes());

        return new String(encryptedBytes, "UTF-8");
    }

    private byte[] hexToByteArray(String hex){
        if(hex == null || hex.length() % 2 != 0){
            return new byte[]{};
        }

        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i < hex.length(); i += 2){
            byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            bytes[(int) Math.floor(i / 2)] = value;
        }

        return bytes;
    }
    /**
     * Base64 엔코딩된 공용키키 문자열로부터 PublicKey객체를 얻는다.
     * @param keyString
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey getPublicKeyFromBase64String(final String keyString)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        final String publicKeyString =
                keyString.replaceAll("\\n",  "").replaceAll("-{5}[ a-zA-Z]*-{5}", "");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKeyString.getBytes());

        return keyFactory.generatePublic(keySpecX509);
    }

    @SneakyThrows
    public static void main(String [] args) {

        // RSA 키쌍을 생성합니다.

        KeyPair keyPair = CipherUtil.genRSAKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String plainText = "EVALI";

        // Base64 인코딩된 암호화 문자열 입니다.
        String encrypted = CipherUtil.encryptRSA(plainText, publicKey);
        System.out.println("encrypted : " + encrypted);

        // 복호화 합니다.
        String decrypted = CipherUtil.decryptRSA(encrypted, privateKey);
        System.out.println("decrypted : " + decrypted);

        // 공개키를 Base64 인코딩한 문자일을 만듭니다.
        byte[] bytePublicKey = publicKey.getEncoded();
        String base64PublicKey = Base64.getEncoder().encodeToString(bytePublicKey);
        System.out.println("Base64 Public Key : " + base64PublicKey);

        // 개인키를 Base64 인코딩한 문자열을 만듭니다.
        byte[] bytePrivateKey = privateKey.getEncoded();
        String base64PrivateKey = Base64.getEncoder().encodeToString(bytePrivateKey);
        System.out.println("Base64 Private Key : " + base64PrivateKey);

        // 문자열로부터 PrivateKey와 PublicKey를 얻습니다.
        PrivateKey prKey = CipherUtil.getPrivateKeyFromBase64String(base64PrivateKey);
        PublicKey puKey = CipherUtil.getPublicKeyFromBase64String(base64PublicKey);
        plainText = "1111";
        // 공개키로 암호화 합니다.
        String encrypted2 = CipherUtil.encryptRSA(plainText, puKey);
        System.out.println("encrypted : " + encrypted2);

        // 복호화 합니다.
        String decrypted2 = CipherUtil.decryptRSA(encrypted2, prKey);
        System.out.println("decrypted : " + decrypted2);
    }
}
