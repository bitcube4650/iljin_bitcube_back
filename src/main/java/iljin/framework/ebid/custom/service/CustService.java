package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustService {
    @Transactional
    public ResultBody save(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        return resultBody;
    }
    @Transactional
    public ResultBody delete(Map<String, String> params) {
        ResultBody resultBody = new ResultBody();
        if (true) {
            resultBody.setCode("ERR");
            resultBody.setMsg("test");
        }
        return resultBody;
    }
}
