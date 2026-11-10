package com.guying.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guying.dto.ProductQuery;
import com.guying.mapper.ProductMapper;
import com.guying.pojo.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ProductTools {

    @Autowired
    private ProductMapper productMapper;

    @Tool(name = "queryProducts", description = "根据条件查询商品信息")
    public List<Product> queryProducts(@ToolParam(required = false, description = "商品查询条件的封装对象") ProductQuery productQuery) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (productQuery == null) {
            queryWrapper.eq("status", 0).last("limit 10");
            return productMapper.selectList(queryWrapper);
        }
        log.info("queryProducts: {}", productQuery);
        queryWrapper.like(productQuery.getTitle() != null, "title", productQuery.getTitle())
                .eq(productQuery.getCategoryId() != null, "category_id", productQuery.getCategoryId())
                .ge(productQuery.getMinPrice() != null, "price", productQuery.getMinPrice())
                .le(productQuery.getMaxPrice() != null, "price", productQuery.getMaxPrice())
                .eq("status", 0) // 确保只查在售商品
                .last("limit 10");
        // 如果没有任何查询条件，返回所有商品
        if (queryWrapper.isEmptyOfWhere()) {
            return productMapper.selectList(null);
        }
        return productMapper.selectList(queryWrapper);
    }
}

