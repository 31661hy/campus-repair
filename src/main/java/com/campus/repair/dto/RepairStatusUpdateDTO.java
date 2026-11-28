package com.campus.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改报修单状态的请求参数。
 */
@Data
public class RepairStatusUpdateDTO {

    @NotBlank(message = "目标状态不能为空")
    private String status;
}
