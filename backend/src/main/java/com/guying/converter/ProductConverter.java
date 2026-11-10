package com.guying.converter;

import com.guying.dto.ProductDTO;
import com.guying.dto.ProductVO;
import com.guying.pojo.Product;
import com.guying.pojo.ProductDoc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.elasticsearch.core.SearchHit;

@Mapper(componentModel = "spring")
public interface ProductConverter {
    Product toProduct(ProductDTO.SaveRequest saveRequest);
    ProductVO.myListResponse toProductMyList(Product product);
    @Mapping(target = "id", expression = "java(String.valueOf(product.getId()))")
    ProductDoc toProductDoc(Product product);
    @Mapping(target = "id", expression = "java(Integer.valueOf(productDoc.getId()))")
    ProductVO.ListResponse toListResponse(ProductDoc productDoc);
}
