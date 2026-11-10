package com.guying.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;// 1: '书籍', 2: '数码', 3: '生活', 4: '体育', 99: '其它'
    private Integer userId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String imageUrl;
    private Integer status;//(0 - 在售，1 - 已售出，2 - 已下架)
    @Version
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
