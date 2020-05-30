package utils;

public enum ActionType {
	// Board actions
	LINE, CIRCLE, RECT, TEXT, ERASER,
	// User-listening actions
	USER_LIST_UPDATE, JOIN_REQUEST_APPROVED, JOIN_REQUEST_DECLINED, KICKED_OUT, MANAGER_EXIT, FILE_NEW, FILE_OPEN,
	// Manager-listening actions
	JOIN_REQUEST;
}
