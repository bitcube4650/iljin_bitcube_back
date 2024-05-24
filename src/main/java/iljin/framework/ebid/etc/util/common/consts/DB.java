package iljin.framework.ebid.etc.util.common.consts;

public class DB {
	
	// 로그인
//	public final static String QRY_INSERT_IMAGE_FILE = "common.insertImageFile";
	public final static String QRY_SELECT_LOGIN_USER_INFO = "login.selectLoginUserInfo";
	public final static String QRY_SELECT_LOGIN_USER_TOKEN_INFO = "login.selectLoginUserTokenInfo";
	public final static String QRY_SELECT_LOGIN_USER_SEARCH = "login.selectLoginUserSearch";
	public final static String QRY_UPDATE_LOGIN_USER_SEARCH_PWD = "login.updateLoginUserSearchPwd";
	public final static String QRY_SELECT_LOGIN_USER_SEARCH_NEW_PWD = "login.selectLoginUserSearchNewPwd";
	public final static String QRY_SELECT_LOGIN_USER_INFO_DETAIL = "login.selectLoginUserInfoDetail";
	public final static String QRY_SELECT_INTERRELATED_LIST = "login.selectInterrelatedList";
	public final static String QRY_SELECT_CUSTOM_USER_DETAILS = "login.selectCustomUserDetails";
	
	// 공통
	public final static String QRY_SELECT_COMMON_CUST_USER_DETAIL = "common.selectCustUserDetail";
	public final static String QRY_SELECT_COMMON_CO_USER_DETAIL = "common.selectCoUserDetail";
	
	
	// 품목
	public final static String QRY_SELECT_ITEM_GRP_LIST = "item.selectItemGrpList";
	public final static String QRY_SELECT_ITEM_LIST = "item.selectItemList";
	public final static String QRY_SELECT_ITEM_CNT = "item.selectItemList_count";
	public final static String QRY_INSERT_ITEM = "item.insertItem";
	public final static String QRY_UPDATE_ITEM = "item.updateItem";
	
	// 메인
	public final static String QRY_SELECT_BID_NOTICING_CNT = "main.selectBidNoticingCnt";
	public final static String QRY_SELECT_BID_SUBMITTED_CNT = "main.selectBidSubmittedCnt";
	public final static String QRY_SELECT_BID_AWARDED_CNT = "main.selectBidAwardedCnt";
	public final static String QRY_SELECT_BID_UNSUCCESSFUL_CNT = "main.selectBidUnsuccessfulCnt";
	public final static String QRY_SELECT_COMPLETE_POSTED_CNT = "main.selectCompletePostedCnt";
	public final static String QRY_SELECT_COMPLETE_SUBMITTED_CNT = "main.selectCompleteSubmittedCnt";
	public final static String QRY_SELECT_COMPLETE_AWARDED_CNT = "main.selectCompleteAwardedCnt";
	public final static String QRY_SELECT_GROUP_PWD_EDIT_DATE = "main.selectGroupPwdEditDate";
	public final static String QRY_SELECT_PWD_CHG_DATE = "main.selectPwdChgDate";
	public final static String QRY_SELECT_INTER_CUST_CODE_LIST = "main.selectInterCustCodeList";
	public final static String QRY_SELECT_MAIN_CO_BID_CNT = "main.selectMainCoBidCnt";
	public final static String QRY_SELECT_PARTNER_CNT = "main.selectPartnerCnt";
	public final static String QRY_SELECT_CO_USER_CNT = "main.selectCoUserCnt";
	public final static String QRY_UPDATE_CO_USER_PASSWORD = "main.updateCoUserPassword";
	public final static String QRY_UPDATE_CO_CUST_USER_PASSWORD = "main.updateCoCustUserPassword";
	public final static String QRY_SELECT_CO_USER_DETAIL = "main.selectCoUserDetail";
	public final static String QRY_SELECT_CO_CUST_USER_DETAIL = "main.selectCoCustUserDetail";
	public final static String QRY_UPDATE_CO_USER_DETAIL = "main.updateCoUserDetail";
	public final static String QRY_UPDATE_CO_CUST_USER_DETAIL = "main.updateCoCustUserDetail";
	
	// cust
	public final static String QRY_SELECT_DUP_USER_CNT = "cust.selectDupUserCnt";
	public final static String QRY_SELECT_CUST_LIST = "cust.selectTCoCustList";
	public final static String QRY_SELECT_OTHER_CUST_LIST = "cust.selectOtherCustList";
	public final static String QRY_SELECT_CUST_DETAIL = "cust.selectTCoCustDetail";
	public final static String QRY_INSERT_CUST_MASTER = "cust.insertTCoCustMaster";
	public final static String QRY_UPDATE_CUST_MASTER = "cust.updateTCoCustMaster";
	public final static String QRY_UPDATE_CUST_CERT = "cust.updateTCoCustMasterCert";
	public final static String QRY_DELETE_CUST_MASTER = "cust.deleteTCoCustMaster";
	public final static String QRY_MERGE_CUST_IR = "cust.mergeTCoCustIR";
	public final static String QRY_DELETE_CUST_IR = "cust.deleteTCoCustIr";
	public final static String QRY_INSERT_CUST_HIST = "cust.insertTCoCustHistory";
	
	// user
	public final static String QRY_SELECT_CUST_USER_LIST = "user.selectTCoCustUserList";
	public final static String QRY_SELECT_CUST_USER_DETAIL = "user.selectTCoCustUserDetail";
	public final static String QRY_INSERT_CUST_USER = "user.insertTCoCustUser";
	public final static String QRY_UPDATE_CUST_USER = "user.updateTCoCustUser";
	public final static String QRY_UPDATE_CUST_USER_USEYN = "user.updateTCoCustUserUseYn";
	public final static String QRY_DELETE_CUST_USER = "user.deleteTCoCustUser";
	public final static String QRY_SELECT_ADMIN_USER_LIST = "user.selectAdminUserList";
	
	// notice
	public final static String QRY_SELECT_NOTICE_LIST = "etc.selectNoticeList";
	public final static String QRY_INSERT_NOTICE = "etc.insertNotice";
	public final static String QRY_INSERT_NOTICE_CUST = "etc.insertNoticeCust";
	public final static String QRY_UPDATE_NOTICE = "etc.updateNotice";
	public final static String QRY_UPDATE_NOTICE_BCNT = "etc.updateNoticeBCount";
	public final static String QRY_DELETE_NOTICE = "etc.deleteNotice";
	public final static String QRY_DELETE_NOTICE_CUST = "etc.deleteNoticeCust";
	
	// FAQ
	public final static String QRY_SELECT_FAQ_LIST = "etc.selectFaqList";
	public final static String QRY_INSERT_FAQ = "etc.insertFaq";
	public final static String QRY_UPDATE_FAQ = "etc.updateFaq";
	public final static String QRY_DELETE_FAQ = "etc.deleteFaq";
	
	//bid_status
	public final static String QRY_SELECT_EBID_STATUS_LIST = "bidStatus.selectEbidStatusList";
	public final static String QRY_SELECT_EBID_STATUS_DETAIL = "bidStatus.selectEbidStatusDetail";
	public final static String QRY_SELECT_EBID_STATUS_JOIN_CUST_LIST = "bidStatus.selectEbidStatusJoinCustList";
	public final static String QRY_SELECT_EBID_STATUS_JOIN_CUST_SPEC = "bidStatus.selectEbidStatusJoinCustSpec";
	public final static String QRY_SELECT_EBID_STATUS_DETAIL_FILE = "bidStatus.selectEbidStatusDetailFile";
	public final static String QRY_SELECT_EBID_STATUS_DETAIL_SPEC = "bidStatus.selectEbidStatusDetailSpec";
	public final static String QRY_UPDATE_EBID_STATUS = "bidStatus.updateEbidStatus";
	public final static String QRY_SELECT_EBID_BI_MODE_A_SEND_INFO = "bidStatus.selectEbidBiModeASendInfo";
	public final static String QRY_SELECT_EBID_BI_MODE_B_SEND_INFO = "bidStatus.selectEbidBiModeBSendInfo";
	public final static String QRY_SELECT_DECRYPT_EBID_CUST_LIST = "bidStatus.selectDecryptEbidCustList";
	public final static String QRY_INSERT_T_BI_DETAIL_MAT_CUST = "bidStatus.insertTBiDetailMatCust";
	public final static String QRY_INSERT_T_BI_DETAIL_MAT_CUST_TEMP = "bidStatus.insertTBiDetailMatCustTemp";
	public final static String QRY_UPDATE_OPEN_EBID_T_BI_INFO_MAT_CUST = "bidStatus.updateOpenEbidTBiInfoMatCust";
	public final static String QRY_UPDATE_OPEN_EBID_T_BI_INFO_MAT = "bidStatus.updateOpenEbidTBiInfoMat";
	public final static String QRY_UPDATE_EBID_SUCCESS_T_BI_INFO_MAT = "bidStatus.updateEbidSuccessTBiInfoMat";
	public final static String QRY_UPDATE_EBID_SUCCESS_T_BI_INFO_MAT_CUST = "bidStatus.updateEbidSuccessTBiInfoMatCust";
	public final static String QRY_UPDATE_EBID_SUCCESS_T_BI_INFO_MAT_CUST_TEMP = "bidStatus.updateEbidSuccessTBiInfoMatCustTemp";
	public final static String QRY_SELECT_EBID_BI_MODE_A_SEND_INFO_CUST_LIST = "bidStatus.selectEbidBiModeASendInfoCustList";
	public final static String QRY_SELECT_EBID_BI_MODE_B_SEND_INFO_CUST_LIST = "bidStatus.selectEbidBiModeBSendInfoCustList";
	public final static String QRY_INSERT_T_BI_INFO_MAT_HIST = "bidStatus.insertTBiInfoMatHist";
	public final static String QRY_UPDATE_REBID_T_BI_INFO_MAT = "bidStatus.updateRebidTBiInfoMat";
	public final static String QRY_UPDATE_REBID_ATT_N = "bidStatus.updateRebidAttN";
	public final static String QRY_DELETE_T_BI_DETAIL_MAT_CUST_CUST_CODE = "bidStatus.deleteTBiDetailMatCustCustCode";
	public final static String QRY_UPDATE_REBID_T_BI_INFO_MAT_CUST_CUST_CODE = "bidStatus.updateRebidTBiInfoMatCustCustCode";
	public final static String QRY_INSERT_T_BI_INFO_MAT_CUST_TEMP = "bidStatus.insertTBiInfoMatCustTemp";
	public final static String QRY_SELECT_T_BI_INFO_MAT_CUST_TEMP_CUST_CODE = "bidStatus.selectTBiInfoMatCustTempCustCode";
	public final static String QRY_UPDATE_OPEN_ATT_SIGN = "bidStatus.updateOpenAttSign";
	public final static String QRY_SELECT_T_BI_INFO_MAT_INFOMATION = "bidStatus.selectTBiInfoMatInfomation";
	public final static String QRY_BID_STATUS_INSERT_T_BI_LOG = "bidStatus.insertTBiLog";
	public final static String QRY_SELECT_PARTNER_EBID_STATUS_LIST = "bidStatus.selectPartnerEbidStatusList";
	public final static String QRY_UPDATE_EBID_T_BI_INFO_MAT_CUST_CONFIRM = "bidStatus.updateEbidTBiInfoMatCustConfirm";
	public final static String QRY_SELECT_PARTNER_EBID_STATUS_DETAIL = "bidStatus.selectPartnerEbidStatusDetail";
	public final static String QRY_SELECT_CODE_RATE_LIST = "bidStatus.selectCodeRateList";
	public final static String QRY_INSERT_T_BI_UPLOAD_C = "bidStatus.insertTBiUploadC";
	public final static String QRY_MERGED_T_BI_INFO_MAT_CUST = "bidStatus.mergedTBiInfoMatCust";
	
	//BID_COMPLETE
	public final static String QRY_SELECT_COMPLETE_EBID_LIST = "bidComp.selectCompleteEbidList";
	public final static String QRY_SELECT_COMPLETE_EBID_DETAIL = "bidComp.selectCompleteEbidDetail";
	public final static String QRY_SELECT_COMPLETE_EBID_JOIN_CUST_LIST = "bidComp.selectCompleteEbidJoinCustList";
	public final static String QRY_SELECT_COMPLETE_EBID_JOIN_CUST_SPEC = "bidComp.selectCompleteEbidJoinCustSpec";
	public final static String QRY_SELECT_COMPLETE_EBID_DETAIL_FILE = "bidComp.selectCompleteEbidDetailFile";
	public final static String QRY_SELECT_COMPLETE_EBID_DETAIL_SPEC = "bidComp.selectCompleteEbidDetailSpec";
	public final static String QRY_UPDATE_COMPLETE_EBID_REAL_AMT = "bidComp.updateCompleteEbidRealAmt";
	public final static String QRY_SELECT_COMPLETE_EBID_LOTTE_MAT_CODE = "bidComp.selectCompleteEbidLotteMatCode";
	public final static String QRY_SELECT_COMPLETE_EBID_HISTORY_LIST = "bidComp.selectCompleteEbidHistoryList";
	public final static String QRY_SELECT_EBID_HISTORY_JOIN_CUST_LIST = "bidComp.selectEbidHistoryJoinCustList";
	public final static String QRY_SELECT_PARTNER_COMPLETE_EBID_LIST = "bidComp.selectPartnerCompleteEbidList";
	public final static String QRY_SELECT_PARTNER_COMPLETE_EBID_DETAIL = "bidComp.selectPartnerCompleteEbidDetail";
	public final static String QRY_SELECT_PARTNER_COMPLETE_EBID_CUST_DETAIL = "bidComp.selectPartnerCompleteEbidCustDetail";
	public final static String QRY_UPDATE_SUCC_EBID_CONFIRM = "bidComp.updateSuccEbidConfirm";
	
}
