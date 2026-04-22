package com.guying.converter;

import com.guying.dto.OrderVO;
import com.guying.pojo.Orders;
import com.guying.pojo.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderConverter {
    OrderVO.ListResponse toListResponse(Orders orders);
    OrderVO.SoldListResponse toSoldListResponse(Orders orders);
    OrderVO.DetailResponse toDetailResponse(OrderItem orderItem);
}
