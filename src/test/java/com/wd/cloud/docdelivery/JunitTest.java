package com.wd.cloud.docdelivery;

import cn.hutool.core.lang.Console;
import org.junit.Test;
import org.springframework.data.domain.Page;

import java.util.Date;

/**
 * @Author: He Zhigang
 * @Date: 2019/6/6 10:11
 * @Description:
 */
public class JunitTest {

    @Test
    public void test1(){
        Page<String> ps = Page.empty();
        Page<String> pss = ps.map(s-> s+"1");
        Console.log(pss);
    }
}
