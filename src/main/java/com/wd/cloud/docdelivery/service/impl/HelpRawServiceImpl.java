package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.wd.cloud.docdelivery.pojo.dto.HelpRawDTO;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.HelpRawRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.repository.VHelpRawRepository;
import com.wd.cloud.docdelivery.repository.VHelpRecordRepository;
import com.wd.cloud.docdelivery.service.HelpRawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static jdk.nashorn.internal.runtime.GlobalFunctions.anonymous;

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
    public void addHelpRaw(Boolean anonymous, Long helpChannel, String helperEamil, String helperIp, String helperName, String orgFlag, String orgName, String info) {
        HelpRaw helpRaw = new HelpRaw();
        List<HelpRaw> helpRaws =new ArrayList<>();
        helpRaw.setAnonymous(anonymous);
        helpRaw.setHelpChannel(helpChannel);
        helpRaw.setHelperEmail(helperEamil);
        helpRaw.setHelperIp(helperIp);
        helpRaw.setHelperName(helperName);
        helpRaw.setOrgFlag(orgFlag);
        helpRaw.setOrgName(orgName);
        helpRaw.setInfo(info);
        helpRaw.setGmtCreate(new Date());
        helpRaw.setGmtModified(new Date());
        helpRaws.add(helpRaw);
        helpRawRepository.saveAll(helpRaws);
    }

    @Override
    public List<HelpRaw> findByIdHelpRaw(Long id) {
        return helpRawRepository.findByIdHelpRaw(id);
    }

    @Override
    public void updateHelpRecordId(Long id, Long helpRecordId) {
        Date gmtModified = new Date();
        Integer invalid = 0;
        if(helpRecordId > 0 && helpRecordId != null){
             invalid = 2;
        }else {
             invalid = 1;
        }
        helpRawRepository.updateHelpRecordId(id,helpRecordId,invalid,gmtModified);
    }

    @Override
    public Page<VHelpRaw> findHelpRaw(Date beginTime,Date endTime,Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, Long helpRecordId, Integer invalid, Pageable pageable) {
        Page<VHelpRaw> vHelpRaws = vHelpRawRepository.findAll(VHelpRawRepository.SpecBuilder.findVhelpRaw(beginTime,endTime ,anonymous, helpChannel, helperEmail, helperIp, helperName,orgFlag,helpRecordId,invalid), pageable);
        return vHelpRaws;
    }

    @Override
    public Page<VHelpRaw> getHelpRaws(String helperName,Long helpRecordId,Date beginTime,Date endTime,Boolean isDifficult,Integer invalid,List<Integer> status,Pageable pageable) {
        Page<VHelpRaw> vHelpRaws = vHelpRawRepository.findAll(VHelpRawRepository.SpecBuilder.buildVhelpRaw(helperName, helpRecordId, beginTime,endTime, isDifficult, invalid, status), pageable);
        return vHelpRaws;
    }

//    private Page<HelpRawDTO> coversHelpRawDTO(Page<VHelpRaw> helpRawPage) {
//        return helpRawPage.map(vHelpRaw -> {
//            HelpRawDTO helpRawDTO = BeanUtil.toBean(anonymous(vHelpRaw), HelpRawDTO.class);
//            Optional<Literature> optionalLiterature = literatureRepository.findById(vHelpRaw.getLiteratureId());
//            optionalLiterature.ifPresent(literature -> helpRawDTO.setDocTitle(literature.getDocTitle()).setDocHref(literature.getDocHref()));
//            return helpRawDTO;
//        });
//    }

}
