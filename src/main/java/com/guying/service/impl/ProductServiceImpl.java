package com.guying.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guying.common.constants.ProductStatus;
import com.guying.converter.ProductConverter;
import com.guying.dto.ProductDTO;
import com.guying.dto.ProductVO;
import com.guying.exception.ServiceException;
import com.guying.mapper.ProductMapper;
import com.guying.pojo.PageResult;
import com.guying.pojo.Product;
import com.guying.pojo.ProductDoc;
import com.guying.service.ProductService;
import com.guying.utils.CacheClient;
import com.guying.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.guying.common.constants.RedisConstants.*;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ElasticsearchOperations operations;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private ProductConverter productConverter;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(ProductDTO.SaveRequest saveRequest) {
        LocalDateTime now = LocalDateTime.now();
        //获取用户id
        Integer userId = UserContext.getUserId();
        //拆箱包装，并设置初始属性
        Product product = productConverter.toProduct(saveRequest);
        if (product.getId() == null) {
            product.setStatus(ProductStatus.ON_SALE);
            product.setCreateTime(now);
            product.setUpdateTime(now);
            product.setUserId(userId);
            productMapper.insert(product);
        }else {
            product.setUpdateTime(now);
            productMapper.updateById(product);
        }
        ProductDoc productDoc = productConverter.toProductDoc(product);
        //保存ES文档
        operations.save(productDoc);
        log.info("用户:{}，保存商品{}",userId, product.getId());
        String key = REDIS_KEY_PREFIX_CACHE_PRODUCT + product.getId();
        String viewCountKey = REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT + product.getId();
        String priceKey=REDIS_KEY_PREFIX_CACHE_PRODUCT_PRICE+product.getId();
        //删除缓存
        if(StringUtils.hasText(key)){
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(viewCountKey);
            stringRedisTemplate.delete(priceKey);
        }
    }

    @Override
    public PageResult<ProductVO.ListResponse> list(ProductDTO.ListRequest listRequest) {
        log.info("查询商品列表");
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolBuilder.must(m -> m.term(t -> t.field("status").value(ProductStatus.ON_SALE)));
        if (StringUtils.hasText(listRequest.getKeyword())){
            boolBuilder.must(m -> m.match(t -> t.field("title").query(listRequest.getKeyword())));
        }
        if(listRequest.getCategoryId() != null){
            boolBuilder.filter(f -> f.term(t -> t.field("categoryId").value(listRequest.getCategoryId())));
        }
        if (listRequest.getMinPrice() != null){
            boolBuilder.filter(f ->f.range(r -> r.number(n -> n.field("price").gte(listRequest.getMinPrice().doubleValue()))));
        }
        if (listRequest.getMaxPrice() != null){
            boolBuilder.filter(f ->f.range(r -> r.number(n -> n.field("price").lte(listRequest.getMaxPrice().doubleValue()))));
        }
        List<Sort.Order> sortOrders = new ArrayList<>();
        if(listRequest.getSort() != null){
            String[] sorts = listRequest.getSort().split(",");
            String[] orders = listRequest.getOrder().split(",");
            for(int i = 0; i < sorts.length; i++){
                String sort = sorts[i];
                Sort.Direction direction = orders[i].equals("asc")?Sort.Direction.ASC:Sort.Direction.DESC;
                if (sort.equals("create_time")){
                    sort = "createTime";
                }
                sortOrders.add(new Sort.Order(direction,sort));
            }
        }
        Sort sort;
        if (sortOrders.isEmpty()){
            sort = Sort.unsorted();
        }
        else {
            sort= Sort.by(sortOrders);
        }
        PageRequest pageRequest = PageRequest.of(listRequest.getPage()-1, listRequest.getSize(),sort);
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withPageable(pageRequest)
                .build();
        SearchHits<ProductDoc> productDocSearchHits = operations.search(query, ProductDoc.class);
        long totalHits = productDocSearchHits.getTotalHits();
        List<ProductVO.ListResponse> listResponse = productDocSearchHits.stream()
                .map(m -> productConverter.toListResponse(m.getContent())).toList();
        /*
        PageHelper.startPage(listRequest.getPage(), listRequest.getSize());
        //解析排序字段
        List<ProductDTO.OrderBy> orderBys = new ArrayList<>();
        if(listRequest.getSort() != null){
            String[] sorts = listRequest.getSort().split(",");
            String[] orders = listRequest.getOrder().split(",");
            for(int i = 0; i < sorts.length; i++){
                ProductDTO.OrderBy orderBy = new ProductDTO.OrderBy();
                orderBy.setColumn(sorts[i]);
                orderBy.setAscOrDesc(orders[i]);
                orderBys.add(orderBy);
            }
        }
        //返回VO
        List<ProductVO.ListResponse> listResponse = productMapper.list(listRequest, orderBys);
        PageInfo<ProductVO.ListResponse> pageInfo = new PageInfo<>(listResponse);
        return new PageResult<>(pageInfo.getTotal(), listResponse);
*/
        return new PageResult<>(totalHits, listResponse);
    }

    @Override
    public List<ProductVO.myListResponse> myList() {
        Integer userId = UserContext.getUserId();
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getUserId, userId));
        return products.stream().map(product -> productConverter.toProductMyList(product)).collect(Collectors.toList());
    }


    @Override
    public ProductVO.DetailResponse detail(Integer id) {
        return cacheClient.queryWithMutex(REDIS_KEY_PREFIX_CACHE_PRODUCT,id, ProductVO.DetailResponse.class,
                                            dbId->productMapper.detail(dbId),30L,TimeUnit.MINUTES);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        productMapper.deleteById(id);
        operations.delete(id.toString(), IndexCoordinates.of("product"));
        String key = REDIS_KEY_PREFIX_CACHE_PRODUCT + id;
        String viewCount = REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT+id;
        stringRedisTemplate.delete(key);
        stringRedisTemplate.delete(viewCount);
    }

    @Override
    public Product getProductById(Integer id) {
        return productMapper.selectById(id);
    }

    @Override
    public void updateStatus(Integer status, Integer id) {
        Product product =productMapper.selectById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        int i = productMapper.updateById(product);
        if (i != 1) {
            throw new ServiceException("商品不存在");
        }
    }
}
