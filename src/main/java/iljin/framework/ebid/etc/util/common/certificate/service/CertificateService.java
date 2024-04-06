package iljin.framework.ebid.etc.util.common.certificate.service;

import java.io.FileInputStream;

import org.springframework.stereotype.Service;

import tradesign.crypto.provider.JeTS;
import tradesign.pki.pkix.EnvelopedData;
import tradesign.pki.pkix.SignedData;
import tradesign.pki.util.JetsUtil;

@Service
public class CertificateService {
	
	//envelope 암호화
	public String encryptData(String data, String interrelatedCustCode) {
		String encrypted = "";
		
		try {
    		// 설정파일 위치를 ebid.jar 경로를 기준으로 상대 경로 지정
            String relativePath = "./data/tradesign3280.properties";
			JeTS.installProvider(relativePath);
			
			//EnvelopedData 객체 생성
			EnvelopedData ed = new EnvelopedData(data.getBytes(), null);
			
			// 계열사의 인증서 설정
			String certPath = "./data/config/ServerCert/";
			certPath += interrelatedCustCode;//입찰에 해당하는 계열사 코드
			certPath += "/kmCert.der";
			
			// 해당 경로의 인증서 불러오기
			FileInputStream fis = new FileInputStream(certPath);
			byte[] cert = new byte[fis.available()];
			fis.read(cert); fis.close();
			
			// 계열사의 인증서 set
			ed.addRecipient(cert);
			
			// byte[] 배열 형식의 envelope 데이타 생성
			byte[] env_msg = ed.envelop();
			
			//DB에 저장할 envelope 데이타(base64 형식)
			encrypted = new String(JetsUtil.encodeBase64(env_msg));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return encrypted;
	}
	
	//envelope 복호화
	public String decryptData(String data, String interrelatedCustCode, String certPwd) {
		String decrypted = "";
		
		try {
    		// 설정파일 위치를 ebid.jar 경로를 기준으로 상대 경로 지정
            String relativePath = "./data/tradesign3280.properties";
			JeTS.installProvider(relativePath);
			
			String beforeDec = new String(data.getBytes("ISO-8859-1"));
			
			//EnvelopedData 객체 생성
			EnvelopedData ed = new EnvelopedData(JetsUtil.decodeBase64(beforeDec.getBytes()));
			
			// 계열사의 인증서 설정
			String certPath = "./data/config/ServerCert/";
			certPath += interrelatedCustCode;//입찰에 해당하는 계열사 코드
			certPath += "/kmCert.der";
			
			// 계열사의 키 설정
			String keyPath = "./data/config/ServerCert/";
			keyPath += interrelatedCustCode;//입찰에 해당하는 계열사 코드
			keyPath += "/kmPri.key";
			
			// 해당 경로의 인증서 불러오기
			FileInputStream fis = new FileInputStream(certPath);
			byte[] cert = new byte[fis.available()];
			fis.read(cert); fis.close();
			
			// 해당 경로의 키 불러오기
			FileInputStream fis2 = new FileInputStream(keyPath);
			byte[] key = new byte[fis2.available()];
			fis2.read(key); fis2.close();
			
			// 수신자의 비밀키와 암호 설정
			ed.setupCipher(cert, key, certPwd);
		
			// Devlope 후 원본데이타 추출
			byte[] dev_msg = ed.getContent();
		
			//화면에 표시할 devlope 데이타
			decrypted = new String(JetsUtil.encodeBase64(dev_msg));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return decrypted;
	}
	
	//서버인증서로 signData 생성
	public String signData(String data) {
		String signed = "";
		try {
			// 설정파일 위치를 ebid.jar 경로를 기준으로 상대 경로 지정
            String relativePath = "./data/tradesign3280.properties";
			JeTS.installProvider(relativePath);
			
			// 서명 암호화
			SignedData sd = new SignedData(data.getBytes(), true);	
			
			// 인증서 설정(일진 씨앤에스)
			String certPath = "./data/config/ServerCert/11/signCert.der";
			String keyPath = "./data/config/ServerCert/11/signPri.key";
			
			// 해당 경로의 인증서, 키 불러오기
			FileInputStream fis = new FileInputStream(certPath);
			byte[] cert = new byte[fis.available()];
			fis.read(cert); fis.close();
			
			FileInputStream fis2 = new FileInputStream(keyPath);
			byte[] key = new byte[fis2.available()];
			fis2.read(key); fis2.close();
			
			sd.setsignCert(cert, key, JeTS.getServerSignKeyPassword(0));
			
			byte[] signed_msg = sd.sign();
			signed = new String(JetsUtil.encodeBase64(signed_msg));
						
		} catch (Exception e1) {
			e1.printStackTrace();
		}			
		
		return signed;
	}
	
	//signData 복구(검증)
	public String signDataFix(String data) {
		String fixed = "";
		try {
			// 설정파일 위치를 ebid.jar 경로를 기준으로 상대 경로 지정
            String relativePath = "./data/tradesign3280.properties";
			JeTS.installProvider(relativePath);
			
			byte[] signed_msg = JetsUtil.decodeBase64(data.getBytes("ISO-8859-1"));
			
			SignedData sd = new SignedData(signed_msg);
			
			byte[] veryfi_msg = sd.getContent();
			fixed = new String(veryfi_msg);
						
		} catch (Exception e1) {
			e1.printStackTrace();
		}			
		
		return fixed;
	}

}
