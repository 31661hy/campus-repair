package com.campus.repair.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.repair.entity.Category;

public interface CategoryService extends IService<Category> {

    Page<Category> pageQuery(long current, long size);

    Category create(Category category);

    /**
     * 修改分类：校验存在、名称不与其他分类重复，返回更新后的分类。
     */
    Category update(Long id, Category category);
}