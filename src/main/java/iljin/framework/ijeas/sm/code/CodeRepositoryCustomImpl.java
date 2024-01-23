package iljin.framework.ijeas.sm.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMapping;

import iljin.framework.core.util.Util;
import org.qlrm.mapper.JpaResultMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import iljin.framework.core.util.Pair;

//@SqlResultSetMapping(name="PairResult", classes = {
//	@ConstructorResult(targetClass = Pair.class, 
//	columns = {@ColumnResult(name="key"), @ColumnResult(name="value")})
//})
@Repository
public class CodeRepositoryCustomImpl implements CodeRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	@SuppressWarnings("rawtypes")
	public List<Pair> getUsedComboByCodeDto(CodeDto codeDto) {
		
		StringBuilder sb = new StringBuilder(" select detail_cd as 'key', detail_nm as 'value' from tb_code_dt where use_yn = 'Y' ");
		if(!StringUtils.isEmpty(codeDto.getCompCd())) {
			sb.append(" and comp_cd = :compCd ");
		}
		if(!StringUtils.isEmpty(codeDto.getGroupCd())) {
			sb.append(" and group_cd = :groupCd ");
		}
		if(!StringUtils.isEmpty(codeDto.getRemark1())) {
			sb.append(" and remark1 = :remark1 ");
		}
		if(!StringUtils.isEmpty(codeDto.getRemark2())) {
			sb.append(" and remark2 = :remark2 ");
		}
		if(!StringUtils.isEmpty(codeDto.getRemark3())) {
			sb.append(" and remark3 like :remark3 ");
		}
		sb.append(" order by order_seq");
		
		Query query = entityManager.createNativeQuery(sb.toString());
		if(!StringUtils.isEmpty(codeDto.getCompCd())) {
			query.setParameter("compCd", codeDto.getCompCd());
		}
		if(!StringUtils.isEmpty(codeDto.getGroupCd())) {
			query.setParameter("groupCd", codeDto.getGroupCd());
		}
		if(!StringUtils.isEmpty(codeDto.getRemark1())) {
			query.setParameter("remark1", codeDto.getRemark1());
		}
		if(!StringUtils.isEmpty(codeDto.getRemark2())) {
			query.setParameter("remark2", codeDto.getRemark2());
		}
		if(!StringUtils.isEmpty(codeDto.getRemark3())) {
			query.setParameter("remark3", "%"+codeDto.getRemark3()+"%");
		}
		
		return new JpaResultMapper().list(query, Pair.class);
	}

	@Override
	public List<CodeDto> getGroupCodeList(CodeDto codeDto) {
		String compCd = codeDto.getCompCd();
		String groupCd = codeDto.getGroupCd();
		String useYn = codeDto.getUseYn();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT" +
				"    HD.COMP_CD," +
				"    HD.GROUP_CD," +
				"    HD.GROUP_NM," +
				"    HD.USE_YN," +
				"    HD.GROUP_DESC" +
				"  FROM TB_CODE_HD HD" +
				" WHERE 1 = 1" +
				"   AND HD.COMP_CD = :compCd" +
				"   AND (HD.GROUP_CD LIKE CONCAT('%', :groupCd, '%') OR HD.GROUP_NM LIKE CONCAT('%', :groupCd, '%'))" +
				"   AND HD.USE_YN LIKE CONCAT('%', :useYn, '%')" +
				" ORDER BY HD.GROUP_CD ASC");

		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("compCd", compCd);
		query.setParameter("groupCd", groupCd);
		query.setParameter("useYn", useYn);

		return new JpaResultMapper().list(query, CodeDto.class);
	}

	@Override
	public List<CodeDto> getGroupCodeDetailList(CodeDto codeDto) {
		String compCd = codeDto.getCompCd();
		String groupCd = codeDto.getGroupCd();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT" +
				"    DT.COMP_CD," +
				"    DT.GROUP_CD," +
				"    DT.DETAIL_CD," +
				"    DT.DETAIL_NM," +
				"    DT.USE_YN," +
				"    DT.ORDER_SEQ," +
				"    DT.DETAIL_DESC," +
				"    DT.REMARK1," +
				"    DT.REMARK2," +
				"    DT.REMARK3," +
				"    DT.REMARK4," +
				"    DT.REMARK5" +
				"  FROM TB_CODE_DT DT" +
				" WHERE 1=1" +
				"   AND DT.COMP_CD = :compCd" +
				"   AND DT.GROUP_CD = :groupCd" +
				" ORDER BY DT.ORDER_SEQ ASC");

		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("compCd", compCd);
		query.setParameter("groupCd", groupCd);

		return new JpaResultMapper().list(query, CodeDto.class);
	}

	@Override
	public Optional<CodeDto> getGroupCodeDetailCode(CodeDto codeDto) {
		String compCd = codeDto.getCompCd();
		String detailCd = codeDto.getDetailCd();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT" +
				"    DT.COMP_CD," +
				"    DT.GROUP_CD," +
				"    DT.DETAIL_CD," +
				"    DT.DETAIL_NM," +
				"    DT.USE_YN," +
				"    DT.ORDER_SEQ," +
				"    DT.DETAIL_DESC," +
				"    DT.REMARK1," +
				"    DT.REMARK2," +
				"    DT.REMARK3," +
				"    DT.REMARK4," +
				"    DT.REMARK5" +
				"  FROM TB_CODE_DT DT" +
				" WHERE 1=1" +
				"   AND DT.COMP_CD = :compCd" +
				"   AND DT.GROUP_CD = 'TYPE_CODE'" +
				"   AND DT.USE_YN = 'Y'" +
				"   AND DT.DETAIL_CD = :detailCd" +
				" ORDER BY DT.ORDER_SEQ ASC");

		Query query = entityManager.createNativeQuery(sb.toString());
		query.setParameter("compCd", compCd);
		query.setParameter("detailCd", detailCd);

		return Optional.ofNullable(new JpaResultMapper().list(query, CodeDto.class).stream().findFirst().orElse(null));
	}

}
