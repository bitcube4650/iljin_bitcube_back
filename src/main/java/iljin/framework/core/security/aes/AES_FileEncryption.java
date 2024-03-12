package iljin.framework.core.security.aes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;

public class AES_FileEncryption {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY = "626974637562656669676874696E6721"; // 16, 24, or 32 bytes

    public static void encryptFile(String filePath) throws Exception {

        //파일 경로를 Path 객체로 변환
        Path path = Paths.get(filePath);

        //파일 이름 추출
        String fileName = path.getFileName().toString();

        //파일 경로 추출
        String directoryPath = path.getParent().toString();

        String encryptedFilePath = directoryPath +  "/" + "test" + fileName;

        File originalFile = new File(filePath);
        File encryptedFile = new File(encryptedFilePath);

        Key secretKey = new SecretKeySpec(toBytes(KEY, 16), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Get the initialization vector
        byte[] iv = cipher.getIV();
        try (FileInputStream inputStream = new FileInputStream(originalFile);
             FileOutputStream outputStream = new FileOutputStream(encryptedFile)) {
            // Write the IV to the beginning of the file
            outputStream.write(iv);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (encryptedBytes != null) {
                    outputStream.write(encryptedBytes);
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                outputStream.write(finalBytes);
            }
        }

        // 기존 원본 파일 삭제
        Files.delete(Paths.get(filePath));

        // 암호화된 파일 이름을 기존 원본 파일의 이름으로 변경
        Files.move(Paths.get(encryptedFilePath), Paths.get(filePath));
    }

    public static String decryptFile(String filePath) throws Exception {
        //파일 경로를 Path 객체로 변환
        Path path = Paths.get(filePath);

        //파일 이름 추출
        String fileName = path.getFileName().toString();

        //파일 경로 추출
        String directoryPath = path.getParent().toString();

        //복호화 파일 경로 생성
        String decryptedFilePath = directoryPath + "/tempDir/" + fileName;

        File encryptedFile = new File(filePath);
        File decryptedFile = new File(decryptedFilePath);

        Key secretKey = new SecretKeySpec(toBytes(KEY, 16), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] iv;
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            iv = new byte[16];
            inputStream.read(iv); // Read the IV from the beginning of the file
        }

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        try (FileInputStream inputStream = new FileInputStream(encryptedFile);
             FileOutputStream outputStream = new FileOutputStream(decryptedFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (decryptedBytes != null) {
                    outputStream.write(decryptedBytes);
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                outputStream.write(finalBytes);
            }
        }

        return decryptedFilePath;
    }



    /**
     * <p>문자열을 바이트배열로 바꾼다.</p>
     *
     * @param digits 문자열
     * @param radix 진수
     * @return
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     */
    public static byte[] toBytes(String digits, int radix) throws IllegalArgumentException, NumberFormatException {
        if (digits == null) {
            return null;
        }
        if (radix != 16 && radix != 10 && radix != 8) {
            throw new IllegalArgumentException("For input radix: \"" + radix + "\"");
        }
        int divLen = (radix == 16) ? 2 : 3;
        int length = digits.length();
        if (length % divLen == 1) {
            throw new IllegalArgumentException("For input string: \"" + digits + "\"");
        }
        length = length / divLen;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int index = i * divLen;
            bytes[i] = (byte)(Short.parseShort(digits.substring(index, index+divLen), radix));
        }
        return bytes;
    }
}
