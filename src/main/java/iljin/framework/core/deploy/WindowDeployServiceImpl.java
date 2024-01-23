package iljin.framework.core.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WindowDeployServiceImpl implements WindowDeployService {
    private static final Logger logger = LoggerFactory.getLogger(WindowDeployServiceImpl.class);

    @Override
    public ResponseEntity<String> getCallDeployService() {
        try {
            Runtime.getRuntime().exec("D:/ijdia-eacct/startup-back.bat");

            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());

            return new ResponseEntity<>("failed", HttpStatus.OK);
        }
    }
}
