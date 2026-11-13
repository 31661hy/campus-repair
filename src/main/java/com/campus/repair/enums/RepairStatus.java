package com.campus.repair.enums;

/**
 * 报修单状态枚举
 */
public enum RepairStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    DONE("已完成"),
    CANCELLED("已取消");

    private final String description;

    RepairStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}