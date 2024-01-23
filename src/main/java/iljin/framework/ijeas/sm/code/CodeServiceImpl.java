package iljin.framework.ijeas.sm.code;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import iljin.framework.core.security.user.User;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import iljin.framework.core.util.Pair;
import iljin.framework.core.util.Util;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CodeServiceImpl implements CodeService {

	private final CodeHeaderRepository codeHeaderRepository;
    private final CodeDetailRepository codeDetailRepository;
    private final CodeRepositoryCustom codeRepositoryCustom;
    private final Util util;

	private static final Logger logger = LoggerFactory.getLogger(CodeService.class);
	
	@Override
	public List<CodeDetail> getCodeDetailAll() {
		return codeDetailRepository.findAll();
	}

	@Override
	public List<Pair> getComboBox(CodeDto codeDto) {
		codeDto.setCompCd(util.getLoginCompCd());
		return codeRepositoryCustom.getUsedComboByCodeDto(codeDto);
//		return codeDetailRepository.getByGroupCdAndRemark1(util.getLoginCompCd(), codeDto.getGroupCd(), codeDto.getRemark1());
	}
    
	@Override
	public List<Map> getByCodeDto(CodeDto codeDto) {
		if(StringUtils.isEmpty(codeDto.getRemark3())) {
			return codeDetailRepository.findByGroupCd(util.getLoginCompCd(), codeDto.getGroupCd());
		} else {
			return codeDetailRepository.findByGroupCdAndRemark3(util.getLoginCompCd(), codeDto.getGroupCd(), codeDto.getRemark3());
		}
	}
	
	@Override
	public List<Map> getCodeAll() {
		return codeDetailRepository.getCodeAll();
	}

	@Override
	public List<CodeDto> getGroupCodeList(CodeDto codeDto) {
		codeDto.setCompCd(util.getLoginCompCd());

		return codeRepositoryCustom.getGroupCodeList(codeDto);
	}

	@Override
	public List<CodeDto> getGroupCodeDetailList(CodeDto codeDto) {

		return codeRepositoryCustom.getGroupCodeDetailList(codeDto);
	}

	@Override
	public ResponseEntity<String> saveCodeLists(CodeHeaderDetials codeHeaderDetials) {
		User loginUser = util.getLoginUser();
		String loginId = loginUser.getLoginId();
		String compCd = loginUser.getCompCd();

		List<CodeDto> codeHeaders = codeHeaderDetials.getCodeHeader();
		List<CodeDto> codeDetails = codeHeaderDetials.getCodeDetail();

		/* 그룹코드 영역(헤더) 처리 */
		for(CodeDto header : codeHeaders) {
			String groupCd = header.getGroupCd();
			String codeCompCd = header.getCompCd();

			CodeHeaderKey codeHeaderKey = new CodeHeaderKey();
			codeHeaderKey.setGroupCd(groupCd);
			codeHeaderKey.setCompCd(codeCompCd);

			Optional<CodeHeader> codeHeader = codeHeaderRepository.findById(codeHeaderKey);

			if(codeHeader.isPresent()) {
				/* update */
				codeHeader.ifPresent(c -> {
					c.setGroupNm(header.getGroupNm());
					c.setUseYn(header.getUseYn());
					c.setGroupDesc(header.getGroupDesc());
					c.setChgId(loginId);
					c.setChgDtm(LocalDateTime.now());

					codeHeaderRepository.save(c);
				});
			} else {
				/* new Insert */
				CodeHeader c = new CodeHeader();
				try {
					PropertyUtils.copyProperties(c, header);
				} catch (Exception e) {
					e.printStackTrace();
				}
				c.setRegId(loginId);
				c.setRegDtm(LocalDateTime.now());
				c.setChgId(loginId);
				c.setChgDtm(LocalDateTime.now());

				codeHeaderRepository.save(c);
			}
		}

		/*
		* 상세코드 영역(상세) 처리
		* Desc. 해당 회사코드/그룹코드로 전체 삭제 후 새로 추가
		* */
		String detailGroupCd = codeDetails.get(0).groupCd;
		codeDetailRepository.deleteByCompCdAndGroupCd(compCd, detailGroupCd);
		for(CodeDto detail : codeDetails) {
			CodeDetail c = new CodeDetail();
			try {
				PropertyUtils.copyProperties(c, detail);
			} catch (Exception e) {
				e.printStackTrace();
			}
			c.setRegId(loginId);
			c.setRegDtm(LocalDateTime.now());
			c.setChgId(loginId);
			c.setChgDtm(LocalDateTime.now());

			codeDetailRepository.save(c);
		}

		return new ResponseEntity<>("저장되었습니다.", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deleteCode(String groupCd) {
		String compCd = util.getLoginCompCd();

		/* delete Detail codes */
		codeDetailRepository.deleteByCompCdAndGroupCd(compCd, groupCd);

		/* delete Header Code */
		CodeHeaderKey codeHeaderKey = new CodeHeaderKey();
		codeHeaderKey.setCompCd(compCd);
		codeHeaderKey.setGroupCd(groupCd);
		codeHeaderRepository.deleteById(codeHeaderKey);

		return new ResponseEntity<>("삭제되었습니다.", HttpStatus.OK);
	}

	@Override
	public Optional<CodeDto> getGroupCodeDetailCode(CodeDto codeDto) {
		return codeRepositoryCustom.getGroupCodeDetailCode(codeDto);
	}

}
