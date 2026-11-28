package com.campus.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增处理记录的请求参数。
 * <p>
 * status 必填：本接口会“写一条记录 + 同事务把报修单改成新状态”，
 * 这是课程要求的事务场景。
 */
@Data
public class RepairRecordCreateDTO {

    @NotBlank(message = "处理人姓名不能为空")
    @Size(max = 50, message = "处理人姓名不能超过 50 个字符")
    private String handlerName;

    @NotBlank(message = "处理说明不能为空")
    @Size(max = 500, message = "处理说明不能超过 500 个字符")
    private String content;

    @NotBlank(message = "目标状态不能为空")
    private String status;
}
