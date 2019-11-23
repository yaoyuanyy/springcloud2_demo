package com.skyler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import sun.misc.ProxyGenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Description:
 * <p></p>
 * <pre>
 *
 *   NB.
 * </pre>
 * <p>
 * Created by skyler on 2019-02-26 at 18:20
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.skyler")
public class ConsumerFeignApp {

    public static void main(String[] args) {

        // 将jvm中的代理对象输出到硬盘的.class文件 方法一
        // --该设置用于输出cglib动态代理产生的类
//        String user_dir = System.getProperty("user.dir");
//        System.out.println("user_dir:" + user_dir);
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, user_dir + "/cglib_proxy/");

        // --该设置用于输出jdk动态代理产生的类，输出的文件路径为your project下。如我的项目是java_example, $ProxyX.class在java_example/com/sun/proxy/下
        // 不起作用
        // 方法1
        System.getProperties().setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

        // 方法2 参考 https://segmentfault.com/a/1190000011608393

        // 方法3 getProxyClassFile()


        SpringApplication.run(ConsumerFeignApp.class, args);
    }


}
