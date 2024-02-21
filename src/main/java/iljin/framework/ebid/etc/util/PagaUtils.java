package iljin.framework.ebid.etc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

@Slf4j
public class PagaUtils {
    public static Pageable pageable(Map params) {
        int page = 0;
        int size = 5;
        if (params.get("page") != null) {
            if (params.get("page") instanceof Integer) {
                page = (int) params.get("page");
            } else {
                try {
                    page = Integer.parseInt(params.get("page").toString());
                } catch (Exception e) {}
            }
        }
        if (params.get("size") != null) {
            if (params.get("size") instanceof Integer) {
                size = (int) params.get("size");
            } else {
                try {
                    size = Integer.parseInt(params.get("size").toString());
                } catch (Exception e) {}
            }
        }
        log.info("page:{}, size:{}", page, size);
        return PageRequest.of(page, size);
    }
    public static Pageable pageable(Map params, String sortName) {
        return pageable(params, sortName, false);
    }
    public static Pageable pageable(Map params, String sortName, boolean Asceding) {
        int page = 0;
        int size = 5;
        if (params.get("page") != null) {
            if (params.get("page") instanceof Integer) {
                page = (int) params.get("page");
            } else {
                try {
                    page = Integer.parseInt(params.get("page").toString());
                } catch (Exception e) {}
            }
        }
        if (params.get("size") != null) {
            if (params.get("size") instanceof Integer) {
                size = (int) params.get("size");
            } else {
                try {
                    size = Integer.parseInt(params.get("size").toString());
                } catch (Exception e) {}
            }
        }
        log.info("page:{}, size:{}", page, size);
        if (Asceding) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortName));
        } else {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortName));
        }
    }
}
