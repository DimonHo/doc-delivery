package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.wd.cloud.commons.exception.NotFoundException;
import com.wd.cloud.docdelivery.model.HelpRawModel;
import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import com.wd.cloud.docdelivery.repository.HelpRawRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.repository.VHelpRawRepository;
import com.wd.cloud.docdelivery.service.HelpRawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author Hu Langshi
 * @date 2019/10/15
 * @Description:
 */
@Slf4j
@Service("helpRaw")
@Transactional(rollbackFor = Exception.class)
public class HelpRawServiceImpl implements HelpRawService {

    @Autowired
    HelpRawRepository helpRawRepository;

    @Autowired
    VHelpRawRepository vHelpRawRepository;

    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    HttpServletRequest request;

    @Override
    public void addHelpRaw(HelpRawModel helpRawModel) {
        HelpRaw helpRaw = BeanUtil.toBean(helpRawModel, HelpRaw.class);
        helpRawRepository.save(helpRaw);
    }

    @Override
    public HelpRaw findByIdHelpRaw(Long id) {
        return helpRawRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Override
    public void updateHelpRecordId(Long id, Long helpRecordId) {
        Date gmtModified = new Date();
        Integer invalid = 0;
        if (helpRecordId > 0 && helpRecordId != null) {
            invalid = 2;
        } else {
            invalid = 1;
        }
        helpRawRepository.updateHelpRecordId(id, helpRecordId, invalid, gmtModified);
    }

    @Override
    public Page<VHelpRaw> findHelpRaw(Date beginTime, Date endTime, Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, Long helpRecordId, Integer invalid, Pageable pageable) {
        Page<VHelpRaw> vHelpRaws = vHelpRawRepository.findAll(VHelpRawRepository.SpecBuilder.findVhelpRaw(beginTime, endTime, anonymous, helpChannel, helperEmail, helperIp, helperName, orgFlag, helpRecordId, invalid), pageable);
        return vHelpRaws;
    }

    @Override
    public Page<VHelpRaw> getHelpRaws(String helperName, Long helpRecordId, Date beginTime, Date endTime, Boolean isDifficult, Integer invalid, List<Integer> status, Pageable pageable) {
        Page<VHelpRaw> vHelpRaws = vHelpRawRepository.findAll(VHelpRawRepository.SpecBuilder.buildVhelpRaw(helperName, helpRecordId, beginTime, endTime, isDifficult, invalid, status), pageable);
        return vHelpRaws;
    }

}
