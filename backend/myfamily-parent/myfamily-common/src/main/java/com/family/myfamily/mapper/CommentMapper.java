package com.family.myfamily.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.myfamily.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
