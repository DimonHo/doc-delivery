package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;

import java.util.Date;
import java.util.List;

/**
 * @Author: He Zouqi
 * @Date: 2019/5/10 17:18
 * @Description:
 */
public interface LiteraturePlanService {

    List<LiteraturePlan> findByDate();


    void arrangePerson(LiteraturePlan literaturePlan, String arrange);

}
