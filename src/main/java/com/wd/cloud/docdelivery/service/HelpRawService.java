package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;

import java.util.Date;
import java.util.List;

/**
 * @author Hu Langshi
 * @date 2019/10/15
 * @Description:
 */
public interface HelpRawService {

    void addHelpRaw(Boolean anonymous,Long helpChannel,String helperEamil,String helperIp,String helperName, String orgFlag,String orgName,String info);

    List<HelpRaw> findByIdHelpRaw(Long id);

    List<VHelpRaw> findHelpRaw(Date gmtCreate, Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, String orgName, Long helpRecordId, Integer invalid);

    void updateHelpRecordId(Long id,Long helpRecordId,Integer invalid);

    List<VHelpRaw> myHelpRaw(String helperName, Integer status);
}
