package iljin.framework.ijeas.sm.code;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import iljin.framework.core.util.Pair;

public interface CodeRepositoryCustom {

	List<Pair> getUsedComboByCodeDto(CodeDto codeDto);

    List<CodeDto> getGroupCodeList(CodeDto codeDto);

    List<CodeDto> getGroupCodeDetailList(CodeDto codeDto);

    Optional<CodeDto> getGroupCodeDetailCode(CodeDto codeDto);
}
