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

    /**
     * 判断当前状态能否合法流转到目标状态。
     * <p>
     * 合法：PENDING→PROCESSING、PROCESSING→DONE、PENDING→CANCELLED；
     * 其余（回退、跳过、终态再变更）一律非法。
     */
    public boolean canTransitionTo(RepairStatus target) {
        return switch (this) {
            case PENDING -> target == PROCESSING || target == CANCELLED;
            case PROCESSING -> target == DONE;
            case DONE, CANCELLED -> false;
        };
    }
}