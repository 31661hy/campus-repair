package com.campus.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.repair.common.Result;
import com.campus.repair.dto.RepairCreateDTO;
import com.campus.repair.entity.RepairOrder;
import com.campus.repair.service.RepairService;
import com.campus.repair.vo.RepairDetailVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报修单接口。
 */
@Validated
@RestController
@RequestMapping("/api/repairs")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    /**
     * POST /api/repairs
     * 新增报修单。请求体示例：
     * <pre>{"reporterName":"张小明","reporterPhone":"13800000001","categoryId":1,
     *       "location":"3号教学楼302","description":"空调不制冷"}</pre>
     */
    @PostMapping
    public Result<RepairOrder> create(@Validated @RequestBody RepairCreateDTO dto) {
        return Result.ok("新增成功", repairService.create(dto));
    }

    /**
     * GET /api/repairs?page=1&size=10&status=PENDING&categoryId=1&keyword=空调
     * 分页 + 条件查询。
     */
    @GetMapping
    public Result<Page<RepairOrder>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page 不能小于 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size 不能小于 1")
            @Max(value = 100, message = "size 不能大于 100") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.ok("查询成功", repairService.pageQuery(page, size, status, categoryId, keyword));
    }

    /**
     * GET /api/repairs/{id}
     * 详情：报修单字段 + 分类名称 + 处理记录列表。
     */
    @GetMapping("/{id}")
    public Result<RepairDetailVO> detail(@PathVariable Long id) {
        return Result.ok("查询成功", repairService.getDetail(id));
    }
}
