package com.skyler.rest;

import com.skyler.client.BrandFeignClient;
import com.skyler.client.param.BrandParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.skyler.client.dto.ResultDTO;

/**
 * Description:
 * <pre>
 *
 * </pre>
 * NB.
 *
 * @author skyler
 * Created by on 2019-11-06 at 23:02
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired private BrandFeignClient brandFeignClient;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResultDTO create(@RequestBody BrandParam param) {
        return brandFeignClient.create(param);
    }

    @GetMapping("/query")
    public ResultDTO query(@RequestParam("id") Long id, @RequestParam("code") String code){
        System.out.println(brandFeignClient.getClass().getName());
        return brandFeignClient.query(id, code);
    }
}
