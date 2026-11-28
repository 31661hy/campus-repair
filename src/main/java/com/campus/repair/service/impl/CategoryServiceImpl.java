package com.campus.repair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.repair.common.Result;
import com.campus.repair.entity.Category;
import com.campus.repair.exception.BusinessException;
import com.campus.repair.mapper.CategoryMapper;
import com.campus.repair.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final long MAX_PAGE_SIZE = 100L;

    @Override
    public Page<Category> pageQuery(long current, long size) {
        long safeCurrent = Math.max(current, 1L);
        long safeSize = (size < 1 || size > MAX_PAGE_SIZE) ? 10L : size;

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getId);

        return this.page(new Page<>(safeCurrent, safeSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category create(Category category) {
        if (category == null) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "分类信息不能为空");
        }

        String name = category.getName() == null ? "" : category.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "分类名称不能为空");
        }

        long exists = this.count(new LambdaQueryWrapper<Category>().eq(Category::getName, name));
        if (exists > 0) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "分类名称已存在：" + name);
        }

        category.setId(null);
        category.setName(name);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getEnabled() == null) {
            category.setEnabled(Boolean.TRUE);
        }

        this.save(category);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category update(Long id, Category category) {
        if (category == null) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "分类信息不能为空");
        }

        Category existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "分类不存在：" + id);
        }

        String name = category.getName() == null ? null : category.getName().trim();
        if (name == null || name.isEmpty()) {
            throw new BusinessException(Result.CODE_BAD_REQUEST, "分类名称不能为空");
        }
        if (!name.equals(existing.getName())) {
            long exists = this.count(new LambdaQueryWrapper<Category>()
                    .eq(Category::getName, name)
                    .ne(Category::getId, id));
            if (exists > 0) {
                throw new BusinessException(Result.CODE_BAD_REQUEST, "分类名称已存在：" + name);
            }
        }

        existing.setName(name);
        if (category.getSort() != null) {
            existing.setSort(category.getSort());
        }
        if (category.getEnabled() != null) {
            existing.setEnabled(category.getEnabled());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return this.getById(id);
    }
}