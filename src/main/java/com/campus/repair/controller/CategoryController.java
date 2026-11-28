package com.campus.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.repair.common.Result;
import com.campus.repair.entity.Category;
import com.campus.repair.service.CategoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** GET /api/categories?page=1&size=10 */
    @GetMapping
    public Result<Page<Category>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page 不能小于 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size 不能小于 1")
            @Max(value = 100, message = "size 不能大于 100") long size) {
        Page<Category> result = categoryService.pageQuery(page, size);
        return Result.ok("查询成功", result);
    }

    /** POST /api/categories */
    @PostMapping
    public Result<Category> create(@Validated @RequestBody Category category) {
        Category saved = categoryService.create(category);
        return Result.ok("新增成功", saved);
    }

    /** PUT /api/categories/{id} 修改分类（存在性校验 + 名称查重） */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @Validated @RequestBody Category category) {
        Category updated = categoryService.update(id, category);
        return Result.ok("更新成功", updated);
    }
}