package org.superchat.transaction.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.superchat.transaction.domain.dto.SecureInvokeDTO;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "secure_invoke_record",autoResultMap = true)
public class SecureInvokeRecord {
    public final static byte STATUS_WAIT=1;

    public final static byte STATUS_FAIL=1;

    @TableId (value = "id", type= IdType.AUTO)
    private Long id;
    @TableField(value = "secure_invoke_json",typeHandler = JacksonTypeHandler.class)
    private SecureInvokeDTO secureInvokeDTO;
    @TableField("status")
    @Builder.Default
    private byte status=SecureInvokeRecord.STATUS_WAIT;
    @TableField("next_retry_time")
    @Builder.Default
    private Date nextRetryTime=new Date();
    @TableField("retry_times")
    @Builder.Default
    private Integer retryTimes=0;
    @TableField("fail_reason")
    private String failReason;

    @TableField("max_retry_times")
    private Integer maxRetryTimes;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;
}
