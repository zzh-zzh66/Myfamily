package com.family.myfamily.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.family.myfamily.entity.Memorial;

public interface MemorialService extends IService<Memorial> {

    Page<Memorial> pageWithMemberInfo(Page<Memorial> page, LambdaQueryWrapper<Memorial> wrapper);

    Memorial getByIdWithMemberInfo(Long id);
}