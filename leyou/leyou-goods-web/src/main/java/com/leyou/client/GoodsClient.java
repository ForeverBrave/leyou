package com.leyou.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Brave
 */
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}
