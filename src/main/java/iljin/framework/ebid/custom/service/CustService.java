package iljin.framework.ebid.custom.service;

import iljin.framework.core.dto.ResponseBody;
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
    public ResponseBody save(Map<String, String> params) {
        ResponseBody responseBody = new ResponseBody();
        return responseBody;
    }
    @Transactional
    public ResponseBody delete(Map<String, String> params) {
        ResponseBody responseBody = new ResponseBody();
        return responseBody;
    }
}
