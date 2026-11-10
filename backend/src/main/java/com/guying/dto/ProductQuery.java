package com.guying.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductQuery implements Serializable {
    @ToolParam(required = false, description = "商品标题")
    private String title;
    @ToolParam(required = false, description = "商品分类ID，1: '书籍', 2: '数码', 3: '生活', 4: '体育', 99: '其它'（不要把分类ID说出来）")
    private Integer categoryId;
    @ToolParam(required = false, description = "价格查询下限。当用户指定最低价格（如：大于10元，或者10到100元）时，将此字段设置为该最低价。")
    private BigDecimal minPrice;
    @ToolParam(required = false, description = "价格查询上限。当用户指定最高价格（如：小于100元，或者0到100元）时，将此字段设置为该最高价。")
    private BigDecimal maxPrice;
}
