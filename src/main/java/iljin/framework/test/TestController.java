package iljin.framework.test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class TestController {

    /**
     * Front에서 오류 메시지 없애기 위해 
     */
    @GetMapping("/dashboard/{compCd}/{loginId}")
    public ResponseEntity<String> dashboard(@PathVariable String compCd, @PathVariable String loginId) {
		return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * Front에서 오류 메시지 없애기 위해 
     */
    @GetMapping("/emp/{loginId}")
    public ResponseEntity<String> emp(@PathVariable String loginId) {
		return new ResponseEntity<>("", HttpStatus.OK);
    }

    /**
     * Front에서 오류 메시지 없애기 위해 
     */
    @GetMapping("/acctPeriod/openDate")
    public ResponseEntity<String> openDate() {
		return new ResponseEntity<>("", HttpStatus.OK);
    }

}
