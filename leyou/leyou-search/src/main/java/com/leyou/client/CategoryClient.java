package com.leyou.client;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Brave
 */
@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi {
}
