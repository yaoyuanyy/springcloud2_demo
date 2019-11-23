package com.skyler.rest;

import com.skyler.client.BrandFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.ProxyGenerator;

import java.io.FileOutputStream;

/**
 * Description:
 * <pre>
 *
 * </pre>
 * NB.
 *
 * @author skyler
 * Created by on 2019-11-14 at 17:55
 */
@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @GetMapping("/getProxyFile")
    public void getProxyClassFile() {
        // 通过 ProxyGenerator.generateProxyClass 产生字节码
        byte[] testProxyBytes = ProxyGenerator.generateProxyClass(BrandFeignClient.class.getSimpleName(), new Class[]{BrandFeignClient.class});
        // 将字节码输出到文件，然后我们再反编译它，看看它的内容是什么
        try (FileOutputStream fileOutputStream = new FileOutputStream("~/TestProxy.class")) {
            fileOutputStream.write(testProxyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
