package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Date;
import java.util.List;

/**
 * @author Hu Langshi
 * @date 2019/10/15
 * @Description:
 */
public interface HelpRawService {
    void addHelpRaw(HelpRaw helpRaw);

    VHelpRaw findByIdHelpRaw(Long id);

    Page<VHelpRaw> findHelpRaw(Date beginTime,Date endTime ,Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, Long helpRecordId, Integer invalid,Pageable pageable);

    void updateHelpRecordId(Long id,Long helpRecordId);

    Page<VHelpRaw> getHelpRaws(String helperName,Long helpRecordId,Date beginTime,Date endTime,Boolean isDifficult,Integer isInvalid,List<Integer> status,String invalidStatus,Pageable pageable);
}
