package com.family.myfamily.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.myfamily.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
