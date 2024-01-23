package iljin.framework.ijeas.sm.code;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import iljin.framework.core.util.Error;
import iljin.framework.core.util.Pair;
import iljin.framework.core.util.Util;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CodeController {
	
	private final CodeService codeService;

	@Autowired
    public CodeController(CodeService codeService, Util util) {
		this.codeService = codeService;
	}

	@ExceptionHandler(CodeException.class)
    public ResponseEntity<Error> receiptNotFound(CodeException e) {
        Error error = new Error(2001, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * 공통콤보 조회
     */
    @GetMapping("/code/combo")
    public ResponseEntity<List<Pair>> getComboBox(@ModelAttribute CodeDto codeDto) {
		List<Pair> codes = codeService.getComboBox(codeDto);
        return new ResponseEntity<>(codes, HttpStatus.OK);
    }

    /**
     * 코드상세 조회
     */
    @GetMapping("/code/detail")
    public ResponseEntity<List<Map>> getCodeDetails(@ModelAttribute CodeDto codeDto) {
    	List<Map> codes = codeService.getByCodeDto(codeDto);
    	return new ResponseEntity<>(codes, HttpStatus.OK);
    }
    
    /**
     * 전체 코드 조회
     */
    @GetMapping("/code")
    public ResponseEntity<List<Map>> getCodeAll() {
    	List<Map> codes = codeService.getCodeAll();
    	return new ResponseEntity<>(codes, HttpStatus.OK);
    }

    /**
     * EA-06-01 공통코드관리
     * GroupCode 리스트 조회
     * @param codeDto has two search conditions.
     *                groupCd/groupNm : 그룹코드/명
     *                useYn : 사용유무
     *
     * */
    @PostMapping("/code/list")
    public ResponseEntity<List<CodeDto>> getGroupCodeList(@RequestBody CodeDto codeDto) {
        List<CodeDto> list = codeService.getGroupCodeList(codeDto);

        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    /**
     * EA-06-01 공통코드관리
     * DetailCode 조회 - 그룹코드DIV 셀 클릭 event
     * @param codeDto has two search variables.
     *                groupCd : 그룹코드DIV에서 클릭한 라인의 그룹코드
     *                compCd : 회사코드
     * */
    @PostMapping("/code/detail")
    public ResponseEntity<List<CodeDto>> getGroupCodeDetailList(@RequestBody CodeDto codeDto) {
        List<CodeDto> list = codeService.getGroupCodeDetailList(codeDto);

        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    /**
     * IJD-eAcct 공통코드관리
     * DetailCode 조회 - 그룹코드DIV 셀 클릭 event
     * @param codeDto is 그룹코드DIV에서 클릭한 라인의 그룹코드
     * */
    @PostMapping("/code/detail/search")
    public ResponseEntity<Optional<CodeDto>> getGroupCodeDetail(@RequestBody CodeDto codeDto) {
        Optional<CodeDto> result = codeService.getGroupCodeDetailCode(codeDto);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    /**
     * EA-06-01 공통코드관리
     * 현재화면 저장
     * @param codeHeaderDetials
     * */
    @PutMapping("/code/save")
    public ResponseEntity<String> saveCodeLists(@RequestBody CodeHeaderDetials codeHeaderDetials) {
        return codeService.saveCodeLists(codeHeaderDetials);
    }
    /**
     * EA-06-01 공통코드관리
     * 그룹코드 영역 행삭제
     * @param groupCd
     * */
    @DeleteMapping("/code/delete/{groupCd}")
    public ResponseEntity<String> deleteCode(@PathVariable String groupCd) {
        return codeService.deleteCode(groupCd);
    }

}

@Getter
@Setter
class CodeHeaderDetials {
    List<CodeDto> codeHeader;
    List<CodeDto> codeDetail;
}
