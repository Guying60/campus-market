package com.guying.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guying.dto.OrderVO;
import com.guying.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

}
