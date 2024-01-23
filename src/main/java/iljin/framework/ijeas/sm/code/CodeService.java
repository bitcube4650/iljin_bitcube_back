package iljin.framework.ijeas.sm.code;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import iljin.framework.core.util.Pair;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface CodeService {

	List<CodeDetail> getCodeDetailAll();
	List<Pair> getComboBox(CodeDto codeDto);
	List<Map> getByCodeDto(CodeDto codeDto);
	List<Map> getCodeAll();

	List<CodeDto> getGroupCodeList(CodeDto codeDto);

	List<CodeDto> getGroupCodeDetailList(CodeDto codeDto);

	@Modifying
	@Transactional
	ResponseEntity<String> saveCodeLists(CodeHeaderDetials codeHeaderDetials);

	@Modifying
	@Transactional
	ResponseEntity<String> deleteCode(String groupCd);

    Optional<CodeDto> getGroupCodeDetailCode(CodeDto codeDto);
}
