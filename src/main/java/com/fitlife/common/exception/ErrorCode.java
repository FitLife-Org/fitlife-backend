package com.fitlife.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(404, "KhĂ´ng tĂ¬m tháº¥y thĂ´ng tin ngÆ°á»i dĂ¹ng!"),
    MEMBER_NOT_FOUND(404, "KhĂ´ng tĂ¬m tháº¥y thĂ´ng tin há»™i viĂªn!"),
    PHONE_ALREADY_EXISTS(400, "Sá»‘ Ä‘iá»‡n thoáº¡i nĂ y Ä‘Ă£ Ä‘Æ°á»£c Ä‘Äƒng kĂ½!"),
    EMAIL_ALREADY_EXISTS(400, "Email nĂ y Ä‘Ă£ Ä‘Æ°á»£c dĂ¹ng lĂ m tĂ i khoáº£n!"),
    MEMBER_NO_ACCOUNT(400, "Há»™i viĂªn chÆ°a cĂ³ tĂ i khoáº£n Ä‘Äƒng nháº­p!"),
    INVALID_CREDENTIALS(401, "TĂ i khoáº£n hoáº·c máº­t kháº©u khĂ´ng chĂ­nh xĂ¡c!"),
    UNCATEGORIZED_EXCEPTION(500, "Lá»—i há»‡ thá»‘ng khĂ´ng xĂ¡c Ä‘á»‹nh, vui lĂ²ng thá»­ láº¡i sau!");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}