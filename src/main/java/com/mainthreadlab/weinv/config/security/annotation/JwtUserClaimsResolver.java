package com.mainthreadlab.weinv.config.security.annotation;

import com.mainthreadlab.weinv.commons.Constants;
import com.mainthreadlab.weinv.exception.UnauthorizedException;
import com.mainthreadlab.weinv.commons.Token;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.annotation.Annotation;

import static com.mainthreadlab.weinv.model.enums.ErrorKey.WRONG_USERNAME_OR_PWD;

public class JwtUserClaimsResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return findMethodAnnotation(JwtUserClaims.class, parameter) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        String authorizationHeader = webRequest.getHeader(Constants.AUTHORIZATION);
        JwtDetails jwtDetails = Token.getJwtDetails(authorizationHeader);
        if (jwtDetails == null) {
            throw new UnauthorizedException(WRONG_USERNAME_OR_PWD);
        }
        return jwtDetails;
    }

    private <T extends Annotation> T findMethodAnnotation(Class<T> annotationClass, MethodParameter parameter) {
        T annotation = parameter.getParameterAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Annotation[] annotationsToSearch = parameter.getParameterAnnotations();
        for (Annotation toSearch : annotationsToSearch) {
            annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(), annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

}


