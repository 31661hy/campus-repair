package com.campus.repair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报修单实体，对应表 repair_order。
 * <p>
 * status 存枚举名（PENDING / PROCESSING / DONE / CANCELLED），与数据库 VARCHAR 字段对应。
 */
@Data
@TableName("repair_order")
public class RepairOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 报修单号，唯一，如 BX202611270001 */
    private String repairNo;

    /** 报修人姓名 */
    private String reporterName;

    /** 报修人电话 */
    private String reporterPhone;

    /** 分类 id（repair_category.id） */
    private Long categoryId;

    /** 报修地点 */
    private String location;

    /** 问题描述 */
    private String description;

    /** 状态：PENDING / PROCESSING / DONE / CANCELLED */
    private String status;

    /** 逻辑删除标记：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
