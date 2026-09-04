package com.slackish.common;

import com.slackish.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    public static final String SESSION_UID = "uid";

    private final JwtService jwtService;

    public CurrentUserResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Uid.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session != null) {
            Object uid = session.getAttribute(SESSION_UID);
            if (uid instanceof Long l) {
                return l;
            }
        }
        String auth = request != null ? request.getHeader("Authorization") : null;
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtService.parse(auth.substring(7));
        }
        throw Errors.unauthorized("Authentication required");
    }
}
