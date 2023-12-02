package org.superchat.server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.superchat.server.common.exception.Enum.ErrorEnum;

/**
 * 自定义限流异常
 */
@Data
public class FrequencyControlException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     *  错误码
     */
    protected Integer errorCode;

    /**
     *  错误信息
     */
    protected String errorMsg;

    public FrequencyControlException() {
        super();
    }

    public FrequencyControlException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public FrequencyControlException(ErrorEnum error) {
        super(error.getErrorMsg());
        this.errorCode = error.getErrorCode();
        this.errorMsg = error.getErrorMsg();
    }

    /**
     * @Description 群异常码
     */
    @AllArgsConstructor
    @Getter
    public enum GroupErrorEnum implements ErrorEnum {
        /**
         *
         */
        GROUP_NOT_EXIST(9001, "该群不存在~"),
        NOT_ALLOWED_OPERATION(9002, "您无权操作~"),
        MANAGE_COUNT_EXCEED(9003, "群管理员数量达到上限，请先删除后再操作~"),
        USER_NOT_IN_GROUP(9004, "非法操作，用户不存在群聊中~"),
        NOT_ALLOWED_FOR_REMOVE(9005, "非法操作，你没有移除该成员的权限"),
        NOT_ALLOWED_FOR_EXIT_GROUP(9006, "非法操作，不允许退出大群聊"),
        ;
        private final Integer code;
        private final String msg;

        @Override
        public Integer getErrorCode() {
            return this.code;
        }

        @Override
        public String getErrorMsg() {
            return this.msg;
        }
    }
}
