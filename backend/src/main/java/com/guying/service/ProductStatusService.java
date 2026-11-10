package com.guying.service;

import com.guying.dto.ProductVO;

public interface ProductStatusService {
    Integer getViewCount(Integer id);

    Integer getIdByProductId(Integer productId);

    ProductVO.StatusResponse getPrice(Integer id);
}
