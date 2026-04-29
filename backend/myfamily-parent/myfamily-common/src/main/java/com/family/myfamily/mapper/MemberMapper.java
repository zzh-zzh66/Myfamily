package com.family.myfamily.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.myfamily.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    @Update("UPDATE t_member SET spouse_id = NULL, spouse_name = NULL, updated_by = #{userId} WHERE id = #{memberId}")
    void clearSpouseById(@Param("memberId") Long memberId, @Param("userId") Long userId);

    @Update("UPDATE t_member SET spouse_id = NULL, spouse_name = NULL, updated_by = #{userId} WHERE spouse_id = #{memberId}")
    void clearSpouseBySpouseId(@Param("memberId") Long memberId, @Param("userId") Long userId);

    @Update("UPDATE t_member SET deleted = 1, updated_by = #{userId} WHERE id = #{memberId}")
    void softDeleteById(@Param("memberId") Long memberId, @Param("userId") Long userId);
}
