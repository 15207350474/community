package com.example.community.config;


import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.security.krb5.KrbException;

import java.util.Properties;

/**
 * 生成验证码
 */

@Configuration
public class KaptchaConfig {
    @Bean
    public Producer kaptchaProducer() throws KrbException {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100"); //设置图片宽度
        properties.setProperty("kaptcha.image.height", "40"); //设置图片高度
        properties.setProperty("kaptcha.textproducer.font.size", "32"); //设置字体大小
        properties.setProperty("kaptcha.textproducer.font.color", "black"); //设置字体颜色
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"); //设置字符
        properties.setProperty("kaptcha.textproducer.char.length", "4"); //设置字体个数

        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;

    }
}
