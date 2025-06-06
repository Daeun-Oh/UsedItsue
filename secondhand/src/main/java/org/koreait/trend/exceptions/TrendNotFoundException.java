package org.koreait.trend.exceptions;

import org.koreait.global.exceptions.NotFoundException;

public class TrendNotFoundException extends NotFoundException {
    public TrendNotFoundException() {
        super("NotFound.trend");
    }
}
