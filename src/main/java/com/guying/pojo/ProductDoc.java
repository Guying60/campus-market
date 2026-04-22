package com.guying.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "product")
public class ProductDoc {
    /**
     * 文档id
     */
    @Id
    private String id;
    /**
     * 商品标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;
    /**
     * 商品分类ID
     */
    @Field(type = FieldType.Keyword)
    private Integer categoryId;
    /**
     * 商品价格
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;
    /**
     * 商品原价
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal originalPrice;

    /**
     * 商品图片URL
     */
    @Field(type = FieldType.Keyword,index = false)
    private String imageUrl;
    /**
     * 商品状态
     */
    @Field(type = FieldType.Integer)
    private Integer status;
    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
