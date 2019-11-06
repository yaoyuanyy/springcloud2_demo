package com.skyler.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.skyler.client.dto.ResultDTO;

/**
 * Description:
 * <pre>
 *
 * </pre>
 * NB.
 *
 * @author skyler
 * Created by on 2019-11-06 at 22:51
 */
@FeignClient(value = "${provider.application:serverB}", contextId = "BrandFeignClient")
public interface BrandFeignClient {
    /**
     * @param param
     * @return
     */
    @PostMapping(value = "api/brand/create", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResultDTO create(@RequestBody BrandParam param);
}
