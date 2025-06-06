package org.koreait.global.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.koreait.member.libs.MemberUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class CommonInterceptor implements HandlerInterceptor {
    private final MemberUtil memberUtil;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            /* 로그인 회원 정보 유지 S */
            modelAndView.addObject("isLogin", memberUtil.isLogin());
            modelAndView.addObject("isAdmin", memberUtil.isAdmin());
            modelAndView.addObject("loggedMember", memberUtil.getMember());
            /* 로그인 회원 정보 유지 E */
        }
    }
}

