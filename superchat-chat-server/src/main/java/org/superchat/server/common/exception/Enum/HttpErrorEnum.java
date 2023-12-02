package org.superchat.server.common.exception.Enum;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Charsets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.superchat.server.common.domain.vo.response.ApiResult;
import org.superchat.server.common.exception.Enum.ErrorEnum;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Description: 业务校验异常码
 */
@AllArgsConstructor
@Getter
public enum HttpErrorEnum implements ErrorEnum {
    ACCESS_DENIED(401, "登录失效，请重新登录"),
    BLACK_UID_DENIED(401, "账户已被封禁"),
    BLACK_IP_DENIED(401, "IP已被封禁"),
    ;
    private Integer httpCode;
    private String msg;

    @Override
    public Integer getErrorCode() {
        return httpCode;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }

    public void sendHttpError(HttpServletResponse response) throws IOException {
        response.setStatus(this.getErrorCode());
        ApiResult responseData = ApiResult.fail(this);
        response.setContentType(ContentType.JSON.toString(Charsets.UTF_8));
        response.getWriter().write(JSONUtil.toJsonStr(responseData));
    }
}
