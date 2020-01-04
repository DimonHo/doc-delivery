package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.pojo.vo.PlanVo;

import java.util.List;

/**
 * @Author: He Zouqi
 * @Date: 2019/5/10 17:18
 * @Description:
 */
public interface LiteraturePlanService {

    void addPlan(List<PlanVo> PlanVOs);

    void delPlan(Long id);

    List<LiteraturePlan> findNextLiteraturePlans();

    List<LiteraturePlan> findNowDaysLiteraturePlans();

    /**
     * 当前排班人
     * @return
     */
    LiteraturePlan nowWatch();
}
