package com.campus.repair.vo;

import com.campus.repair.entity.RepairRecord;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修单详情返回对象：报修单字段 + 分类名称 + 处理记录列表。
 */
@Data
public class RepairDetailVO {

    private Long id;

    private String repairNo;

    private String reporterName;

    private String reporterPhone;

    private Long categoryId;

    /** 分类名称（方便前端直接展示，不用再查一次分类） */
    private String categoryName;

    private String location;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 处理记录列表（按时间升序） */
    private List<RepairRecord> records;
}
