package iljin.co.kr.bid.controller;

// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// import lombok.Getter;
// import lombok.Setter;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.codec.CodecException;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import iljin.co.kr.bid.dto.BidDto;
// import iljin.co.kr.bid.service.BidProgressService;
// import iljin.framework.core.util.Error;
// import iljin.framework.core.util.Pair;
// import iljin.framework.core.util.Util;
// import iljin.framework.ijeas.sm.code.CodeDto;

// @RestController
// @CrossOrigin
// @RequestMapping("/api")
// public class BidProgressController {

//     private final BidProgressService bidProgressService;

//     @Autowired
//     public CodeController(BidProgressService bidProgressService, Util util) {
// 		this.bidProgressService = bidProgressService;
// 	}

// 	@ExceptionHandler(CodecException.class)
//     public ResponseEntity<Error> receiptNotFound(CodecException e) {
//         Error error = new Error(2001, e.getMessage());
//         return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//     }

//     @PostMapping("/bid/progresslist")
//     public ResponseEntity<List<Map>> getBidProgresslist(@RequestBody BidDto bidDto) {
//         List<Map> list = bidProgressService.getBidProgresslist(bidDto);

//         return new ResponseEntity<>(list, HttpStatus.OK);
//     }
// }
