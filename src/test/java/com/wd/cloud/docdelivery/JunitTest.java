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
        int x = 12;
        int y = 23;
        x ^= y;
        y = x ^ y;
        x = x^ y;
        System.out.println(x);
        System.out.println(y);
    }
}
