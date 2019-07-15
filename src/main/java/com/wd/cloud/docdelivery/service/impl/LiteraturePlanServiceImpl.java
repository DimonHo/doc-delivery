package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.pojo.vo.PlanVO;
import com.wd.cloud.docdelivery.repository.LiteraturePlanRepository;
import com.wd.cloud.docdelivery.service.LiteraturePlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("literaturePlanService")
@Transactional(rollbackFor = Exception.class)
public class LiteraturePlanServiceImpl implements LiteraturePlanService {

    @Autowired
    LiteraturePlanRepository literaturePlanRepository;

    @Override
    public void addPlan(List<PlanVO> PlanVOs) {
        List<LiteraturePlan> literaturePlans = new ArrayList<>();
        PlanVOs.forEach(p -> literaturePlans.add(BeanUtil.toBean(p, LiteraturePlan.class)));
        literaturePlanRepository.saveAll(literaturePlans);
    }

    @Override
    public void delPlan(Long id) {
        literaturePlanRepository.deleteById(id);
    }

    @Override
    public List<LiteraturePlan> findNextLiteraturePlans() {
        return literaturePlanRepository.findNextLiteraturePlans();
    }

    @Override
    public List<LiteraturePlan> findNowDaysLiteraturePlans() {
        return literaturePlanRepository.findNowDayLiteraturePlans();
    }

}
