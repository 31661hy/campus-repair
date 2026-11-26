-- =====================================================================
-- 校园报修管理系统 · 数据库初始化脚本（3 表版）
-- 文件：init.sql
-- 环境：MySQL 8.0+
-- 说明：可重复执行；执行后会清空并重建 campus_repair 库，只用于开发环境！
-- 表：repair_category（分类）/ repair_order（报修单）/ repair_record（处理记录）
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. 建库
-- ---------------------------------------------------------------------
DROP DATABASE IF EXISTS campus_repair;
CREATE DATABASE campus_repair
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;
USE campus_repair;

-- ---------------------------------------------------------------------
-- 2. 报修分类表 repair_category
--    name 业务上要求唯一，这里用普通索引 + 服务层校验，
--    避免"逻辑删除后同名分类无法再新建"的问题。
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS repair_record;
DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS repair_category;

CREATE TABLE `repair_category` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(50)     NOT NULL                COMMENT '分类名称（业务唯一）',
    `sort`       INT             NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `enabled`    TINYINT(1)      NOT NULL DEFAULT 1      COMMENT '是否启用：1-启用 0-停用',
    `deleted`    TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_name` (`name`),
    KEY `idx_category_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '报修分类表';

-- ---------------------------------------------------------------------
-- 3. 报修单表 repair_order
--    status 存枚举名：PENDING（待处理）/ PROCESSING（处理中）/
--    DONE（已完成）/ CANCELLED（已取消）
-- ---------------------------------------------------------------------
CREATE TABLE `repair_order` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `repair_no`      VARCHAR(32)     NOT NULL                COMMENT '报修单号（唯一），如 BX202611260001',
    `reporter_name`  VARCHAR(50)     NOT NULL                COMMENT '报修人姓名',
    `reporter_phone` VARCHAR(20)     NOT NULL                COMMENT '报修人电话',
    `category_id`    BIGINT UNSIGNED NOT NULL                COMMENT '分类（repair_category.id）',
    `location`       VARCHAR(200)    NOT NULL                COMMENT '报修地点，如 3号教学楼302',
    `description`    TEXT                NULL                COMMENT '问题详细描述',
    `status`         VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/DONE/CANCELLED',
    `deleted`        TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_repair_no` (`repair_no`),
    KEY `idx_order_status_created` (`status`, `created_at`),
    KEY `idx_order_category` (`category_id`),
    CONSTRAINT `fk_order_category` FOREIGN KEY (`category_id`) REFERENCES `repair_category` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '报修单表';

-- ---------------------------------------------------------------------
-- 4. 处理记录表 repair_record
-- ---------------------------------------------------------------------
CREATE TABLE `repair_record` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `repair_id`    BIGINT UNSIGNED NOT NULL                COMMENT '报修单（repair_order.id）',
    `handler_name` VARCHAR(50)     NOT NULL                COMMENT '处理人姓名',
    `content`      VARCHAR(500)    NOT NULL                COMMENT '处理说明',
    `deleted`      TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_record_repair` (`repair_id`),
    CONSTRAINT `fk_record_order` FOREIGN KEY (`repair_id`) REFERENCES `repair_order` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '处理记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 5. 演示数据
-- ---------------------------------------------------------------------

-- 5.1 分类：5 条
INSERT INTO `repair_category` (`id`, `name`, `sort`, `enabled`, `deleted`, `created_at`, `updated_at`) VALUES
(1, '水电维修', 1,  1, 0, '2026-11-24 08:00:00', '2026-11-24 08:00:00'),
(2, '网络故障', 2,  1, 0, '2026-11-24 08:00:00', '2026-11-24 08:00:00'),
(3, '桌椅门窗', 3,  1, 0, '2026-11-24 08:00:00', '2026-11-24 08:00:00'),
(4, '空调制冷', 4,  1, 0, '2026-11-24 08:00:00', '2026-11-24 08:00:00'),
(5, '其他',    99, 1, 0, '2026-11-24 08:00:00', '2026-11-24 08:00:00');

-- 5.2 报修单：6 条，覆盖四种状态
INSERT INTO `repair_order` (`id`, `repair_no`, `reporter_name`, `reporter_phone`, `category_id`, `location`, `description`, `status`, `deleted`, `created_at`, `updated_at`) VALUES
(1, 'BX202611240001', '张小明', '13800000001', 4, '3号教学楼302', '开机后只有风、不制冷，已试过重启无效。', 'PENDING',    0, '2026-11-24 09:10:00', '2026-11-24 09:10:00'),
(2, 'BX202611240002', '李小红', '13800000002', 2, '6号宿舍楼502', '连上 WiFi 后一直提示无网络，室友正常。', 'PROCESSING', 0, '2026-11-24 10:20:00', '2026-11-24 11:00:00'),
(3, 'BX202611250003', '王小刚', '13800000003', 1, '2号宿舍楼208', '洗手台水龙头关不紧，一直滴水。', 'DONE',       0, '2026-11-25 15:30:00', '2026-11-25 17:40:00'),
(4, 'BX202611250004', '陈小雨', '13800000004', 3, '1号教学楼401', '课桌抽屉滑轨坏了，拉不开。', 'PENDING',    0, '2026-11-25 16:00:00', '2026-11-25 16:00:00'),
(5, 'BX202611260005', '李小红', '13800000002', 5, '图书馆三楼', '灯管频繁闪烁，影响自习。', 'CANCELLED',  0, '2026-11-26 08:30:00', '2026-11-26 08:45:00'),
(6, 'BX202611260006', '张小明', '13800000001', 1, '实验楼B207', '插座没电，插排指示灯不亮。', 'PROCESSING', 0, '2026-11-26 09:00:00', '2026-11-26 09:30:00');

-- 5.3 处理记录：3 条
INSERT INTO `repair_record` (`id`, `repair_id`, `handler_name`, `content`, `deleted`, `created_at`) VALUES
(1, 2, '赵师傅', '已联系学生，约定 14:00 上门排查。', 0, '2026-11-24 11:00:00'),
(2, 2, '赵师傅', '更换网线并重置交换机端口，网络恢复正常。', 0, '2026-11-24 14:30:00'),
(3, 3, '赵师傅', '更换水龙头阀芯，漏水问题解决。', 0, '2026-11-25 17:40:00');

-- ---------------------------------------------------------------------
-- 6. 自检
-- ---------------------------------------------------------------------
SELECT 'repair_category' AS table_name, COUNT(*) AS rows_count FROM `repair_category`
UNION ALL
SELECT 'repair_order', COUNT(*) FROM `repair_order`
UNION ALL
SELECT 'repair_record', COUNT(*) FROM `repair_record`;
