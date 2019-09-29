package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
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

    private static int index = 0;

    private static List<LiteraturePlan> oldNowPlans = new ArrayList<>();

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

    @Override
    public LiteraturePlan nowWatch(){
        // 获取当前时间的排班人列表
        List<LiteraturePlan> nowPlans = literaturePlanRepository.findByNowPlans();
        // 如果当前排班人列表为空，则获取下一个时段排班人列表
        if (CollectionUtil.isEmpty(nowPlans)){
            nowPlans = literaturePlanRepository.findByNextPlans();
        }
        // 如果上一次的排班人列表跟这一次的不相同，那么index重新计数
        if (!oldNowPlans.containsAll(nowPlans) || !nowPlans.containsAll(oldNowPlans)) {
            index = 0;
        }

        // 轮询排班
        LiteraturePlan nowWatch = null;
        if (CollectionUtil.isNotEmpty(nowPlans)) {
            nowWatch = nowPlans.get(index);
            index = (index + 1) % nowPlans.size();
        }
        oldNowPlans = nowPlans;
        return nowWatch;
    }


}