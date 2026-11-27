package com.campus.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增报修单的请求参数。
 * <p>
 * 用 DTO 而不是直接拿实体接收请求，避免调用方偷偷传 status / id 等不该由前端决定的字段。
 */
@Data
public class RepairCreateDTO {

    @NotBlank(message = "报修人姓名不能为空")
    @Size(max = 50, message = "报修人姓名不能超过 50 个字符")
    private String reporterName;

    @NotBlank(message = "报修人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "报修人电话格式不正确")
    private String reporterPhone;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "报修地点不能为空")
    @Size(max = 200, message = "报修地点不能超过 200 个字符")
    private String location;

    @Size(max = 500, message = "问题描述不能超过 500 个字符")
    private String description;
}
