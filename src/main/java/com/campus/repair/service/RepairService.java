package com.campus.repair.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.repair.dto.RepairCreateDTO;
import com.campus.repair.entity.RepairOrder;
import com.campus.repair.vo.RepairDetailVO;

/**
 * 报修单业务接口。
 */
public interface RepairService extends IService<RepairOrder> {

    /**
     * 新增报修单（校验分类存在，状态默认 PENDING，单号自动生成）。
     */
    RepairOrder create(RepairCreateDTO dto);

    /**
     * 分页 + 条件查询（状态 / 分类 / 关键字）。
     */
    Page<RepairOrder> pageQuery(long current, long size, String status, Long categoryId, String keyword);

    /**
     * 详情：报修单字段 + 分类名称 + 处理记录列表。
     */
    RepairDetailVO getDetail(Long id);
}
