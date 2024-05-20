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
	
	// 품목
	public final static String QRY_SELECT_ITEM_GRP_LIST = "item.selectItemGrpList";
	public final static String QRY_SELECT_ITEM_LIST = "item.selectItemList";
	public final static String QRY_SELECT_ITEM_LIST_CNT = "item.selectItemListCnt";
	
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
}
