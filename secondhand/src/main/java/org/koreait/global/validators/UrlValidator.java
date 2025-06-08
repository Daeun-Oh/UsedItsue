package org.koreait.global.validators;

public interface UrlValidator {
    /**
     * url 형식 체크
     * @param url
     * @return
     */
    default boolean check(String url) {
        String pattern = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$";
        return url != null && url.matches(pattern);
    }
}
