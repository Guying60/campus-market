package com.guying.controller;

import com.guying.dto.ProductDTO;
import com.guying.dto.ProductVO;
import com.guying.pojo.PageResult;
import com.guying.pojo.Result;
import com.guying.service.ProductService;
import com.guying.service.ProductStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductStatusService productStatusService;

    @PostMapping("/save")
    public Result save(@RequestBody @Validated ProductDTO.SaveRequest saveRequest) {
        log.info("保存商品");
        productService.save(saveRequest);
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(@Validated ProductDTO.ListRequest listRequest) {
        log.info("查询商品列表");
        PageResult<ProductVO.ListResponse> pageResult = productService.list(listRequest);
        return Result.success(pageResult);
    }

    @GetMapping("/my-list")
    public Result myList() {
        log.info("查询我的商品列表");
        List<ProductVO.myListResponse> list = productService.myList();
        return Result.success(list);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        log.info("查询商品详情");
        ProductVO.DetailResponse detailResponse = productService.detail(id);
        return Result.success(detailResponse);
    }

    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        log.info("删除商品");
        productService.delete(id);
        return Result.success();
    }

    @GetMapping("/dynamic/{id}")
    public Result getStats(@PathVariable Integer id){
        Integer viewCount = productStatusService.getViewCount(id);
        ProductVO.StatusResponse response =productStatusService.getPrice(id);
        response.setViewCount(viewCount);
        response.setId(id);
        return Result.success(response);
    }
}
