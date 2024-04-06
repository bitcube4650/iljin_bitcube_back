package iljin.framework.test;

import iljin.framework.ebid.etc.util.common.message.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@CrossOrigin
@Slf4j
public class TestController {


    @Autowired
    private MessageService messageService;

    @GetMapping("/test")
    public void test() {
//        messageService
        LocalDateTime currentDate = LocalDateTime.now();
        String [] datetime = currentDate.toString().split("T");
        String date = datetime[0].replaceAll("-","");
        String time = datetime[1].replaceAll(":","").substring(0, 6);
        log.info(date);
        log.info(time);
        String rPhone = "01012345678";
        String rPhone2 = rPhone.substring(3, rPhone.length());
        String rPhone3 = rPhone2.length() == 7 ? rPhone2.substring(3, rPhone2.length()) : rPhone2.substring(4, rPhone2.length());
        rPhone2 = rPhone2.length() == 7 ? rPhone2.substring(0, 3) : rPhone2.substring(0, 4);
        log.info(rPhone);
        log.info(rPhone2);
        log.info(rPhone3);
        messageService.send("일진그룹", "01099030052", "홍길동", "테스트 메시지입니다.");
    }

}
