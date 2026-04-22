package com.guying.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guying.dto.ProductDTO;
import com.guying.dto.ProductVO;
import com.guying.pojo.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {


    List<ProductVO.ListResponse> list(ProductDTO.ListRequest listRequest, List<ProductDTO.OrderBy> orderBys);


    @Select("select p.id,title,original_price,p.user_id,p.status,category_id,image_url,description," +
            "p.create_time,p.update_time,u.nickname seller_name,u.id seller_id,u.phone seller_phone  " +
            "from product p " +
            "inner join user u on p.user_id = u.id where p.id = #{id}")
    ProductVO.DetailResponse detail(Integer id);




    @Select("select price from product where id=#{id}")
    ProductVO.StatusResponse selectPriceById(Integer id);

}
