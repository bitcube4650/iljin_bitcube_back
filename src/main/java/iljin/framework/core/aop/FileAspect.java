package iljin.framework.core.aop;

import iljin.framework.core.util.Util;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
class FileAspect {
    private static final Logger logger = LoggerFactory.getLogger(FileAspect.class);

    final
    Util util;

    public FileAspect(final Util util) {
        this.util = util;
    }


    @Before("execution(* iljin.framework.core.file.FileController.downloadFile(..))")
    public void authorityCheck(JoinPoint joinPoint) {
        logger.info("================== authorityCheck");
        // TODO
        // 사용자 정보를 가져오고, 다운로드 받으려는 파일이 내부용 보고서인지 체크를 해야 한다.
        // if (사용자정보 == "세일즈") { 다운로드받으려는 파일의 타입이 internal이면 안됨 }
    }

}