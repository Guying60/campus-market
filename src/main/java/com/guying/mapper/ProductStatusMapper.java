package com.guying.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductStatusMapper {


    @Insert("insert into product_status(product_id, view_count) VALUES (#{member},#{viewCount})" )
    void insert(Integer member, Integer viewCount);

    @Select("select id from product_status where product_id=#{productId}")
    Integer selectIdByProductId(Integer productId);

    @Update("update product_status set product_id=#{productId},view_count=#{viewCount} where id = #{id}")
    void update(Integer id, int productId, int viewCount);
}
