package com.guying.service;

import com.guying.dto.ProductDTO;
import com.guying.dto.ProductVO;
import com.guying.pojo.PageResult;
import com.guying.pojo.Product;

import java.util.List;

public interface ProductService {
    void save(ProductDTO.SaveRequest saveRequest);

    PageResult<ProductVO.ListResponse> list(ProductDTO.ListRequest listRequest);

    List<ProductVO.myListResponse> myList();


    ProductVO.DetailResponse detail(Integer id);

    void delete(Integer id);

    Product getProductById(Integer id);

    void updateStatus(Integer status, Integer id);



}
