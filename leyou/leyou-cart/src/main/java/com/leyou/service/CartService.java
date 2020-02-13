package com.leyou.service;

import com.leyou.client.GoodsClient;
import com.leyou.interceptor.LoginInterceptor;
import com.leyou.pojo.Cart;
import com.leyou.pojo.Sku;
import com.leyou.pojo.UserInfo;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "user:cart:";

    public void addCart(Cart cart) {
        
        //从拦截器中获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo(); 
        
        //获取hash操作对象(查询购物车记录)
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //抽取出来key
        String key = cart.getSkuId().toString();
        //未登录时的原有数量
        Integer num = cart.getNum();

        //判断当前商品是否在购物车中
        if(hashOperations.hasKey(key)){
            //在，则更新数量(得到结果为json类型的字符串)
            String cartJson = hashOperations.get(key).toString();
            //JsonUtils工具反序列化为cart对象
            cart = JsonUtils.parse(cartJson, Cart.class);
            //总数 = 登录后的数量加登录前的数量
            cart.setNum(cart.getNum() + num);
            //  hashOperations.put(key,JsonUtils.serialize(cart));
        }else {
            //不在，则新增购物车
            cart.setUserId(userInfo.getId());
            //因为传递过来的只有id，num ， 所以要根据skuId查询其余5个参数 title,ownspec,image,price
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setPrice(sku.getPrice());
            //  hashOperations.put(key,JsonUtils.serialize(cart));
        }
        //统一抽取到if方法之外
        hashOperations.put(key,JsonUtils.serialize(cart));

    }

    /**
     * 查询购物车
     * @return
     */
    public List<Cart> queryCarts() {
        //从拦截器中获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判断用户是否有购物记录
        if(!this.redisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return null;
        }
        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //获取购物车Map中所有Cart值集合
        List<Object> cartsJson = hashOps.values();

        //如果购物车集合为空，直接返回null
        if(CollectionUtils.isEmpty(cartsJson)){
            return null;
        }

        //利用stream表达式将List<Object>集合转化为List<Cart>集合
        return cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(),Cart.class)).collect(Collectors.toList());
    }

    /**
     * 更新购物车数量
     * @param cart
     */
    public void updateNum(Cart cart) {
        //从拦截器中获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断用户是否有购物记录
        if(!this.redisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return ;
        }
        //获取购物车商品数量
        Integer num = cart.getNum();
        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        //反序列化
        cart = JsonUtils.parse(cartJson, Cart.class);
        //赋值购物车商品数量
        cart.setNum(num);
        //更新redis中的数量
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    /**
     * 删除购物车商品
     * @return
     */
    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }
}
