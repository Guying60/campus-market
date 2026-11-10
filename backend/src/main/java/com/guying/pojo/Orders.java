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
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer sellerId;
    private Integer productId;
    private BigDecimal price;
    private Integer status;//(0 - 待付款，1 - 待发货，2 - 待收货，3 - 已完成，4 - 已取消)
    private String address;
    private String customerPhone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String sellerName;
    private String customerName;
    private String sellerPhone;
    private String productTitle;
}
