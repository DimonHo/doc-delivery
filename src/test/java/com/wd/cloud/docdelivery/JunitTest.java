package com.wd.cloud.docdelivery;

import org.junit.Test;

import java.util.Date;

/**
 * @Author: He Zhigang
 * @Date: 2019/6/6 10:11
 * @Description:
 */
public class JunitTest {

    @Test
    public void test1(){
        boolean flag = new Date().compareTo(null) > 0;
    }
}
