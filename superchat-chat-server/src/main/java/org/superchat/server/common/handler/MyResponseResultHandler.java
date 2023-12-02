package org.superchat.server.common.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.common.domain.vo.response.ApiResult;

import java.util.Objects;

@AllArgsConstructor
@Slf4j
@ControllerAdvice
public class MyResponseResultHandler implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (Objects.requireNonNull(returnType.getMethod()).getAnnotatedReturnType().getType().getTypeName()
                .equals(FileSystemResource.class.getTypeName())) {
            return false;
        }
        ResponseResult annotation = Objects.requireNonNull(returnType, "returnType is null").getDeclaringClass().getAnnotation(ResponseResult.class);

        if (Objects.isNull(annotation)) {
            annotation = returnType.getMethod().getAnnotation(ResponseResult.class);
        }
        return Objects.nonNull(annotation) && !annotation.ignore();
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        ApiResult<Object> apiResult = ApiResult.success(body);
        if (Objects.isNull(body) && MediaType.TEXT_HTML_VALUE.equals(selectedContentType.toString())) {
            ObjectMapper om = new ObjectMapper();
            response.getHeaders().set("Content-Type", "application/json");
            return om.writeValueAsString(apiResult);
        }
        return apiResult;
    }
}
