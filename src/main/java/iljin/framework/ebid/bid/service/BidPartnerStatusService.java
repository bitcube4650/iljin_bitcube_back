package iljin.framework.ebid.bid.service;

import iljin.framework.core.dto.ResultBody;
import iljin.framework.core.security.user.UserDto;
import iljin.framework.core.security.user.UserRepository;
import iljin.framework.core.security.user.UserRepositoryCustom;
import iljin.framework.core.util.Util;
import iljin.framework.ebid.bid.dto.BidProgressCustDto;
import iljin.framework.ebid.bid.dto.BidProgressDetailDto;
import iljin.framework.ebid.bid.dto.BidProgressDto;
import iljin.framework.ebid.bid.dto.BidProgressFileDto;
import iljin.framework.ebid.bid.dto.BidProgressTableDto;
import iljin.framework.ebid.bid.dto.CoUserInfoDto;
import iljin.framework.ebid.bid.dto.EmailDto;
import iljin.framework.ebid.bid.dto.InterUserInfoDto;
import iljin.framework.ebid.bid.dto.InterrelatedCustDto;
import iljin.framework.ebid.bid.dto.SubmitHistDto;
import iljin.framework.ebid.custom.entity.TCoCustUser;
import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoCustUserRepository;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.PagaUtils;
import iljin.framework.ebid.etc.util.common.file.FileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import iljin.framework.ebid.etc.util.common.file.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class BidPartnerStatusService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    Util util;

    @Autowired
    private FileService fileService;

    @Autowired
    private BidProgressService bidProgressService;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    public Page statuslist(@RequestBody Map<String, Object> params) {

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        StringBuilder sbCount = new StringBuilder(
                " select count(1) FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.create_user = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id, t_bi_info_mat_cust d  WHERE a.bi_no = d.bi_no and d.cust_code = :custCode  ");
        StringBuilder sbList = new StringBuilder(
                "SELECT a.bi_no AS bi_no, a.bi_name AS bi_name, " +
                        "DATE_FORMAT(a.est_start_date, '%Y-%m-%d %H:%i') AS est_start_date, " +
                        "DATE_FORMAT(a.est_close_date, '%Y-%m-%d %H:%i') AS est_close_date, " +
                        "CASE WHEN a.bi_mode = 'A' THEN '지명' ELSE '일반' END AS bi_mode, " +
                        "CASE WHEN a.ins_mode = '1' THEN '파일' ELSE '직접입력' END AS ins_mode, " +
                        "CASE WHEN a.ing_tag = 'A1' AND d.esmt_yn = '2' THEN '투찰' WHEN a.ing_tag = 'A3' THEN '재입찰' WHEN a.ing_tag = 'A1' THEN '입찰공고' END AS ing_tag, "
                        +
                        "b.user_name AS cuser, b.user_email AS cuser_email, " +
                        "c.user_name AS gongo_id, c.user_email AS gongo_email, " +
                        "a.interrelated_cust_code AS interrelated_cust_code " +
                        "FROM t_bi_info_mat a LEFT JOIN t_co_user b ON a.create_user = b.user_id LEFT JOIN t_co_user c ON a.gongo_id = c.user_id, t_bi_info_mat_cust d "
                        +
                        "WHERE a.bi_no = d.bi_no and d.cust_code = :custCode ");
        StringBuilder sbWhere = new StringBuilder();

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            sbWhere.append(" and a.bi_no = :bidNo ");
        }

        if (!StringUtils.isEmpty(params.get("bidName"))) {
            sbWhere.append(" and a.bi_name like concat('%',:bidName,'%') ");
        }
        // biMode
        if ((Boolean) params.get("bidModeA") || (Boolean) params.get("bidModeB")) {
            sbWhere.append(" and ( ");
            if ((Boolean) params.get("bidModeA") && !(Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'A' ");
            }
            if (!(Boolean) params.get("bidModeA") && (Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'B' ");
            }
            if ((Boolean) params.get("bidModeA") && (Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'A' or a.bi_mode = 'B'");
            }
            if (!(Boolean) params.get("bidModeA") && !(Boolean) params.get("bidModeB")) {
                sbWhere.append(" a.bi_mode = 'C'");
            }
            sbWhere.append(" ) ");
        }

        // ingTag
        if ((Boolean) params.get("noticeYn") || (Boolean) params.get("participateYn")
                || (Boolean) (params.get("rebidYn"))) {

            sbWhere.append(" and ( ");
            if ((Boolean) params.get("noticeYn") && !(Boolean) params.get("participateYn")
                    && !(Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A0' ");
            }
            if ((Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && !(Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A0' or d.esmt_yn = '2' ");
            }
            if ((Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A0' or d.esmt_yn = '2' or a.ing_tag = 'A3' ");
            }
            if ((Boolean) params.get("noticeYn") && !(Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A0' or a.ing_tag = 'A3' ");
            }
            if (!(Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && !(Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" d.esmt_yn = '2' ");
            }
            if (!(Boolean) params.get("noticeYn") && (Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" d.esmt_yn = '2' or a.ing_tag = 'A3' ");
            }
            if (!(Boolean) params.get("noticeYn") && !(Boolean) params.get("participateYn")
                    && (Boolean) (params.get("rebidYn"))) {
                sbWhere.append(" a.ing_tag = 'A3' ");
            }
            sbWhere.append(" ) ");
        } else {// 아무것도 체크하지 않은 경우

            sbWhere.append(" and a.ing_tag = '99' ");
        }

        sbWhere.append(
                "and (a.create_user = :userid " +
                        "or a.open_att1 = :userid " +
                        "or a.open_att2 = :userid " +
                        "or a.gongo_id = :userid " +
                        "or a.est_bidder = :userid " +
                        "or a.est_opener = :userid)");

        sbList.append(sbWhere);
        sbCount.append(sbWhere);

        Query queryList = entityManager.createNativeQuery(sbList.toString());
        Query queryTotal = entityManager.createNativeQuery(sbCount.toString());
        queryList.setParameter("custCode", params.get("custCode"));
        queryTotal.setParameter("custCode", params.get("custCode"));

        if (!StringUtils.isEmpty(params.get("bidNo"))) {
            queryList.setParameter("bidNo", params.get("bidNo"));
            queryTotal.setParameter("bidNo", params.get("bidNo"));
        }
        if (!StringUtils.isEmpty(params.get("bidName"))) {
            queryList.setParameter("bidName", params.get("bidName"));
            queryTotal.setParameter("bidName", params.get("bidName"));
        }
        queryList.setParameter("userid", userId);
        queryTotal.setParameter("userid", userId);

        Pageable pageable = PagaUtils.pageable(params);
        queryList.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize()).getResultList();
        List list = new JpaResultMapper().list(queryList, BidProgressDto.class);

        BigInteger count = (BigInteger) queryTotal.getSingleResult();
        return new PageImpl(list, pageable, count.intValue());
    }

    public void checkBid(@RequestBody Map<String, Object> params) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getUsername();

        String biNo = (String) params.get("biNo");

        StringBuilder updateBid = new StringBuilder(
            "UPDATE t_bi_info_mat_cust set esmt_yn = '1' where bi_no = :biNo and cust_code = :custCode");
        Query updateQ = entityManager.createNativeQuery(updateBid.toString());   
        updateQ.setParameter("biNo", biNo);
        updateQ.setParameter("custCode", (String) params.get("custCode"));

        Map<String, String> logParams = new HashMap<>();
        logParams.put("msg", "[업체]공고확인");
        logParams.put("biNo", biNo);
        logParams.put("userId", userId);
        bidProgressService.updateLog(logParams);
    }
}
