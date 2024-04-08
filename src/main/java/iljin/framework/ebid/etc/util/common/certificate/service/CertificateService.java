package iljin.framework.ebid.etc.util.common.certificate.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.springframework.stereotype.Service;

import iljin.framework.core.dto.ResultBody;
import tradesign.crypto.provider.JeTS;
import tradesign.pki.pkix.EnvelopedData;
import tradesign.pki.pkix.SignedData;
import tradesign.pki.util.JetsUtil;

@Service
public class CertificateService {
	
	//envelope 암호화
	public ResultBody encryptData(String data, String interrelatedCustCode) {
		ResultBody resultBody = new ResultBody();
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
			
			resultBody.setData(encrypted);
			
		} catch (Exception e1) {
			e1.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
			resultBody.setMsg(e1.getMessage());
			
			return resultBody;
		}
		
		return resultBody;
	}
	
	//envelope 복호화
	public ResultBody decryptData(String data, String interrelatedCustCode, String certPwd) {
		ResultBody resultBody = new ResultBody();
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
			
			resultBody.setData(decrypted);
			
		} catch (Exception e1) {
			e1.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
			resultBody.setMsg(e1.getMessage());
		}
		
		return resultBody;
	}
	
	//서버인증서로 signData 생성
	public ResultBody signData(String data) {
		ResultBody resultBody = new ResultBody();
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
			
			resultBody.setData(signed);
						
		} catch (Exception e1) {
			e1.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
			resultBody.setMsg(e1.getMessage());
			
			return resultBody;
		}			
		
		return resultBody;
	}
	
	//signData 복구(검증)
	public ResultBody signDataFix(String data) {
		ResultBody resultBody = new ResultBody();
		String fixed = "";
		try {
			// 설정파일 위치를 ebid.jar 경로를 기준으로 상대 경로 지정
            String relativePath = "./data/tradesign3280.properties";
			JeTS.installProvider(relativePath);
			
			byte[] signed_msg = JetsUtil.decodeBase64(data.getBytes("ISO-8859-1"));
			
			SignedData sd = new SignedData(signed_msg);
			
			byte[] veryfi_msg = sd.getContent();
			fixed = new String(veryfi_msg);
			
			resultBody.setData(fixed);
						
		} catch (Exception e1) {
			e1.printStackTrace();
			resultBody.setCode("ERROR");
			resultBody.setStatus(999);
			resultBody.setMsg(e1.getMessage());
		}			
		
		return resultBody;
	}
	
	//인증서 pem 형태에서 파일 형태로 변환
	public void pemToFile(String filePath) {
		try{
			//pem 데이터의 -----BEGIN CERTIFICATE----- 와 -----END CERTIFICATE----- 사이에 있는 데이터를
			//메모장에 붙여넣기 한 이후에 파일이름.b64 형태로 저장
			//파일이름은 
			//암호용 인증서의 경우 kmCert.b64
			//서명용 인정서의 경우 signCert.b64
			//암호용 키의 경우 kmPri.b64
			//서명용 키의 경우 signPri.b64
			//위와같이 만든 파일의 전체 경로를 파라미터(filePath)로 받음
			
			String filename = filePath;
			FileInputStream fis = new FileInputStream(filename);
			byte[] b64 = new byte[fis.available()];
			fis.read(b64); fis.close();
					
			byte[] loginData = JetsUtil.decodeBase64(b64);
			
			FileOutputStream fos = new FileOutputStream(filename + "_bin");
			fos.write(loginData); fos.close();
			//결과물로 파일이름.b64_bin 파일이 생성됨
			
			//생성된 파일의 이름바꾸기로 확장자 변경
			//인증서는 확장자를 .b64_bin에서 .der로 변경
			//키는 확장자를 .b64_bin에서 .key로 변경
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//인증서 파일 형태에서 pem 형태로 변환
	public void fileToPem(String filePath) {
		
		try {
			String filename = filePath;
			
			FileInputStream fis = new FileInputStream(filename);
			byte[] bin = new byte[fis.available()];
			fis.read(bin); fis.close();			
			
			byte[] b64d = JetsUtil.encodeBase64(bin) ;
			
			FileOutputStream fos = new FileOutputStream(filename + "_1");
			fos.write(b64d); fos.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
