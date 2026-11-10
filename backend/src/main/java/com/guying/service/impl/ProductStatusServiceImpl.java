package com.guying.service.impl;

import com.guying.dto.ProductVO;
import com.guying.mapper.ProductMapper;
import com.guying.mapper.ProductStatusMapper;
import com.guying.service.ProductStatusService;
import com.guying.utils.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.guying.common.constants.RedisConstants.REDIS_KEY_PREFIX_CACHE_PRODUCT_PRICE;
import static com.guying.common.constants.RedisConstants.REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT;
@Slf4j
@Service
public class ProductStatusServiceImpl implements ProductStatusService {
    @Autowired
    private ProductStatusMapper productStatusMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CacheClient cacheClient;

    @Override
    public Integer getViewCount(Integer id) {
        String key =REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT+id;
        String viewCount = stringRedisTemplate.opsForValue().get(key);
        //Redis有，直接返回
        if (StringUtils.hasText(viewCount)) {
            stringRedisTemplate.opsForValue().increment(key);
            //有更新viewCount的商品加入到一个新的set集合，用于更新到DB中
            stringRedisTemplate.opsForSet().add(REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT+"set", String.valueOf(id));
            return Integer.parseInt(viewCount);
        }
        //没有,初始化一个
        stringRedisTemplate.opsForValue().set(key, "1");
        return 1;
    }


    @Scheduled(fixedDelay = 60*1000)
    @Transactional(rollbackFor = Exception.class)
    public void refreshProductStatus() {
        String key = REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT+"set";
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return;
        }
        for (String member : members) {
            try {
                String viewCount = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX_CACHE_PRODUCT_VIEWCOUNT+member);
                if (viewCount == null) {
                    //当商品被修改，删除或创建订单的时候会触发
                    stringRedisTemplate.opsForSet().remove(key, member);
                    continue;
                }
                Integer id = getIdByProductId(Integer.parseInt(member));
                if (id != null) {
                    productStatusMapper.update(id,Integer.parseInt(member),Integer.parseInt(viewCount));
                    stringRedisTemplate.opsForSet().remove(key,member);
                }
                else {
                    productStatusMapper.insert(Integer.parseInt(member), Integer.parseInt(viewCount));
                    stringRedisTemplate.opsForSet().remove(key, member);
                }
            } catch (Exception e) {
                log.error("商品"+member+"更新浏览次数出错了");
            }
        }
            
    }

    @Override
    public Integer getIdByProductId(Integer productId){
        return productStatusMapper.selectIdByProductId(productId);
    }

    @Override
    public ProductVO.StatusResponse getPrice(Integer id) {
        return cacheClient.queryWithMutex(REDIS_KEY_PREFIX_CACHE_PRODUCT_PRICE,id,ProductVO.StatusResponse.class,dbId->productMapper.selectPriceById(dbId),30L,TimeUnit.MINUTES);
    }



}
