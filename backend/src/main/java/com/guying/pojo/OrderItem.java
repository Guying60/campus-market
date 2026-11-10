package com.guying.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String productTitle;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
}
