package com.wd.cloud.docdelivery.service.impl;

import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import com.wd.cloud.docdelivery.repository.HelpRawRepository;
import com.wd.cloud.docdelivery.repository.VHelpRawRepository;
import com.wd.cloud.docdelivery.service.HelpRawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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
    public List<VHelpRaw> findHelpRaw(Date gmtCreate, Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, String orgName, Long helpRecordId, Integer invalid) {
        return vHelpRawRepository.findHelpRaw(gmtCreate,anonymous,helpChannel,helperEmail,helperIp,helperName,orgFlag,orgName,helpRecordId,invalid);
    }

    @Override
    public void updateHelpRecordId(Long id, Long helpRecordId,Integer invalid) {
        Date gmtModified = new Date();
        helpRawRepository.updateHelpRecordId(id,helpRecordId,invalid,gmtModified);
    }

    @Override
    public List<VHelpRaw> myHelpRaw(String helperName, Integer status) {
        return vHelpRawRepository.findByHelperNameAndStatus(helperName,status);
    }


}
