package iljin.framework.ijeas.sm.code;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeDetailRepository extends JpaRepository<CodeDetail, CodeDetailKey> {
	
	@Query(value=" select " +
			" 	detail_cd as 'key', " +
			" 	detail_nm as 'value' " +
			" from " +
			" 	tb_code_dt " +
			" where " +
			" 	comp_cd = :compCd " +
			" 	and group_cd = :groupCd " +
			" 	and remark1 = if(ifnull(:remark1,'')='', remark1, :remark1) ", nativeQuery = true)
	List<Map> getByGroupCdAndRemark1(@Param("compCd") String compCd, @Param("groupCd") String groupCd, @Param("remark1") String remark1);
	
	
	@Query(value=" select " +
			" 	detail_cd as detailCd , " +
			" 	detail_nm as detailNm, " +
			" 	remark1, " +
			" 	remark2, " +
			" 	remark3, " +
			" 	remark4, " +
			" 	remark5 " +
			" from " +
			" 	tb_code_dt " +
			" where " +
			" 	comp_cd = :compCd " +
			" 	and group_cd = :groupCd " +
			" 	and use_yn = 'Y' " +
			" order by order_seq", nativeQuery=true)
	List<Map> findByGroupCd(@Param("compCd") String compCd, @Param("groupCd") String groupCd);

	@Query(value=" select " +
			" 	detail_cd as detailCd , " +
			" 	detail_nm as detailNm, " +
			" 	remark1, " +
			" 	remark2, " +
			" 	remark3, " +
			" 	remark4, " +
			" 	remark5 " +
			" from " +
			" 	tb_code_dt " +
			" where " +
			" 	comp_cd = :compCd " +
			" 	and group_cd = :groupCd " +
			" 	and remark3 like concat('%',:remark3,'%') " +
			" 	and use_yn = 'Y' " +
			" order by order_seq", nativeQuery=true)
	List<Map> findByGroupCdAndRemark3(@Param("compCd") String compCd, @Param("groupCd") String groupCd, @Param("remark3") String remark3);

	@Query(value=" select " +
			" 	detail_cd as detailCd , " +
			" 	detail_nm as detailNm, " +
			" 	remark1, " +
			" 	remark2, " +
			" 	remark3, " +
			" 	remark4, " +
			" 	remark5 " +
			" from " +
			" 	tb_code_dt " +
			" where " +
			" 	comp_cd = :compCd " +
			" 	and group_cd = :groupCd " +
			" 	and remark5 like concat('%',:remark5,'%') " +
			" 	and use_yn = 'Y' ", nativeQuery=true)
	Optional<Map> findTopByGroupCdAndRemark5OrderByOrderSeqAsc(@Param("compCd") String compCd, @Param("groupCd") String groupCd, @Param("remark5") String remark5);
	
	@Query(value="select h.group_cd as groupCd , h.group_nm as groupNm , h.comp_cd as compCd , d.detail_cd as detailCd , d.detail_nm as detailNm, d.use_yn as useYn from tb_code_hd h join tb_code_dt d on d.GROUP_CD = h.GROUP_CD and d.COMP_CD = h.COMP_CD", nativeQuery=true)
	List<Map> getCodeAll();

	void deleteByCompCdAndGroupCd(String compCd, String detailGroupCd);

	Optional<CodeDetail> findByCompCdAndGroupCdAndUseYnAndDetailCd(String compCd, String groupCd, String useYn, String detailCd);

	@Query(value = "" +
			" SELECT V.PRJT_CD as detailCd " +
			"      , V.PRJT_NM as detailNm" +
			"      , CONCAT(SUBSTRING(V.PRJT_STR_DT, 1, 4), '-', SUBSTRING(V.PRJT_STR_DT, 5, 2), '-', SUBSTRING(V.PRJT_STR_DT, 7, 2)) AS remark2 " +
			"      , CONCAT(SUBSTRING(V.PRJT_END_DT, 1, 4), '-', SUBSTRING(V.PRJT_END_DT, 5, 2), '-', SUBSTRING(V.PRJT_END_DT, 7, 2)) as remark3 " +
			"      , V.REMARK1 " +
			"   FROM ALPERP.CBO_EA_PRODUCT_CODE_V V" +
			"  WHERE 1=1 " +
			"    AND V.PRJT_CD IS NOT NULL "+
			"    AND ( V.PRJT_CD LIKE CONCAT('%', IFNULL(:value, ''), '%')" +
			"         OR V.PRJT_NM LIKE CONCAT('%', IFNULL(:value, ''), '%') )" +
			" ORDER BY V.PRJT_CD DESC", nativeQuery = true)
	List<Map> getContractList(@Param("value") String value);

	@Query(value = "" +
			" SELECT V.PRJT_CD as detailCd " +
			"      , V.PRJT_NM as detailNm" +
			"      , V.REMARK1 " +
			"   FROM ALPERP.CBO_EA_PRODUCT_CODE_V V" , nativeQuery = true)
	List<Map> getContractList();
}