package com.elifcelik.stockmanagement.productcacheservice.exception.exceptions;

import com.elifcelik.stockmanagement.productcacheservice.enums.Language;
import com.elifcelik.stockmanagement.productcacheservice.exception.enums.FriendlyMessageCode;
import com.elifcelik.stockmanagement.productcacheservice.exception.utils.FriendlyMessageUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ProductNotCreateException extends RuntimeException{
    private final Language language;
    private final FriendlyMessageCode friendlyMessageCode;

    public ProductNotCreateException(Language language, FriendlyMessageCode friendlyMessageCode, String message) {
        super(FriendlyMessageUtils.getFriendlyMessage(language, friendlyMessageCode));
        this.language = language;
        this.friendlyMessageCode = friendlyMessageCode;
        log.error("[ProductNotCreateException] -> message: {} developer message: {}", FriendlyMessageUtils.getFriendlyMessage(language,friendlyMessageCode), message);
    }
}
