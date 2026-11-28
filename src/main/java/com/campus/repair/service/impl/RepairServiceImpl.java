package com.campus.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.repair.common.Result;
import com.campus.repair.dto.RepairCreateDTO;
import com.campus.repair.dto.RepairRecordCreateDTO;
import com.campus.repair.entity.Category;
import com.campus.repair.entity.RepairOrder;
import com.campus.repair.entity.RepairRecord;
import com.campus.repair.enums.RepairStatus;
import com.campus.repair.exception.BusinessException;
import com.campus.repair.mapper.RepairOrderMapper;
import com.campus.repair.mapper.RepairRecordMapper;
import com.campus.repair.service.CategoryService;
import com.campus.repair.service.RepairService;
import com.campus.repair.vo.RepairDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报修单业务实现。
 */
@Service
public class RepairServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairService {

    /** 单页最多条数，防止前端传超大 size */
    private static final long MAX_PAGE_SIZE = 100L;

    /** 单号前缀日期格式：BX20261127 */
    private static final DateTimeFormatter NO_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RepairRecordMapper repairRecordMapper;
    private final CategoryService categoryService;

    public RepairServiceImpl(RepairRecordMapper repairRecordMapper, CategoryService categoryService) {
        this.repairRecordMapper = repairRecordMapper;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrder create(RepairCreateDTO dto) {
        // 1. 分类必须存在（逻辑删除的分类查不到，等于不存在）
        if (dto.getCategoryId() == null || categoryService.getById(dto.getCategoryId()) == null) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "分类不存在：" + dto.getCategoryId());
        }

        // 2. 组装报修单：状态默认待处理，单号自动生成
        RepairOrder order = new RepairOrder();
        order.setRepairNo(generateRepairNo());
        order.setReporterName(dto.getReporterName().trim());
        order.setReporterPhone(dto.getReporterPhone().trim());
        order.setCategoryId(dto.getCategoryId());
        order.setLocation(dto.getLocation().trim());
        order.setDescription(dto.getDescription());
        order.setStatus(RepairStatus.PENDING.name());

        // 3. 保存后重新查询，把数据库生成的 id / 时间字段回填给前端
        this.save(order);
        return this.getById(order.getId());
    }

    @Override
    public Page<RepairOrder> pageQuery(long current, long size, String status, Long categoryId, String keyword) {
        long safeCurrent = Math.max(current, 1L);
        long safeSize = (size < 1 || size > MAX_PAGE_SIZE) ? 10L : size;

        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();

        // 状态条件：先校验枚举合法性，非法状态直接报参数错误
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(RepairOrder::getStatus, parseStatus(status).name());
        }
        if (categoryId != null) {
            wrapper.eq(RepairOrder::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(RepairOrder::getRepairNo, k)
                    .or().like(RepairOrder::getReporterName, k)
                    .or().like(RepairOrder::getLocation, k)
                    .or().like(RepairOrder::getDescription, k));
        }

        wrapper.orderByDesc(RepairOrder::getCreatedAt)
                .orderByDesc(RepairOrder::getId);

        return this.page(new Page<>(safeCurrent, safeSize), wrapper);
    }

    @Override
    public RepairDetailVO getDetail(Long id) {
        RepairOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "报修单不存在：" + id);
        }

        RepairDetailVO vo = new RepairDetailVO();
        BeanUtils.copyProperties(order, vo);

        // 分类名称：分类被删除时返回 null，不影响详情查看
        Category category = categoryService.getById(order.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());

        // 处理记录按时间升序，展示处理过程
        List<RepairRecord> records = repairRecordMapper.selectList(
                new LambdaQueryWrapper<RepairRecord>()
                        .eq(RepairRecord::getRepairId, id)
                        .orderByAsc(RepairRecord::getCreatedAt)
                        .orderByAsc(RepairRecord::getId));
        vo.setRecords(records);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrder updateStatus(Long id, String targetStatus) {
        RepairOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "报修单不存在：" + id);
        }
        RepairStatus current = parseStatus(order.getStatus());
        RepairStatus target = parseStatus(targetStatus);
        validateTransition(current, target);

        // 只改 status / updatedAt，避免把整行其他字段重写一遍
        this.lambdaUpdate()
                .eq(RepairOrder::getId, id)
                .set(RepairOrder::getStatus, target.name())
                .set(RepairOrder::getUpdatedAt, LocalDateTime.now())
                .update();

        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairRecord addRecord(Long id, RepairRecordCreateDTO dto) {
        RepairOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "报修单不存在：" + id);
        }
        RepairStatus current = parseStatus(order.getStatus());
        RepairStatus target = parseStatus(dto.getStatus());
        validateTransition(current, target);

        // 1. 写处理记录
        RepairRecord record = new RepairRecord();
        record.setRepairId(id);
        record.setHandlerName(dto.getHandlerName().trim());
        record.setContent(dto.getContent().trim());
        repairRecordMapper.insert(record);

        // 2. 同事务更新状态：任何一步失败都会整体回滚（课程要求的事务场景）
        this.lambdaUpdate()
                .eq(RepairOrder::getId, id)
                .set(RepairOrder::getStatus, target.name())
                .set(RepairOrder::getUpdatedAt, LocalDateTime.now())
                .update();

        return repairRecordMapper.selectById(record.getId());
    }

    /** 校验状态流转是否合法，非法抛 400 */
    private void validateTransition(RepairStatus current, RepairStatus target) {
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(Result.CODE_BAD_REQUEST,
                    "不允许从 " + current + " 变更为 " + target);
        }
    }

    /** 把请求里的状态字符串转成枚举，非法值抛 400 */
    private RepairStatus parseStatus(String status) {
        try {
            return RepairStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "状态参数不合法：" + status);
        }
    }

    /**
     * 生成报修单号：BX + 当天日期 + 当天序号（0001 起）。
     * 单人演示场景下串号概率可忽略；如要并发安全，可改用数据库序列表。
     */
    private String generateRepairNo() {
        String prefix = "BX" + LocalDate.now().format(NO_DATE_FORMAT);
        LambdaQueryWrapper<RepairOrder> query = new LambdaQueryWrapper<RepairOrder>()
                .likeRight(RepairOrder::getRepairNo, prefix)
                .orderByDesc(RepairOrder::getRepairNo)
                .last("LIMIT 1");
        RepairOrder last = this.getOne(query, false);

        int seq = 1;
        if (last != null && last.getRepairNo() != null) {
            String tail = last.getRepairNo().substring(prefix.length());
            try {
                seq = Integer.parseInt(tail) + 1;
            } catch (NumberFormatException ignored) {
                // 老数据格式异常时按 1 继续，不阻断新增
            }
        }
        return prefix + String.format("%04d", seq);
    }
}
