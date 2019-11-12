package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.wd.cloud.docdelivery.exception.RepeatHelpRequestException;
import com.wd.cloud.docdelivery.pojo.entity.*;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.service.AsyncService;
import com.wd.cloud.docdelivery.service.HelpRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/28 14:40
 * @Description:
 */
@Slf4j
@Service("helpRequest")
@Transactional(rollbackFor = Exception.class)
public class HelpRequestServiceImpl implements HelpRequestService {

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    AsyncService asyncService;

    @Override
    public void helpRequest(Literature literature, HelpRecord helpRecord) {
        literature.createUnid();
        Optional<Literature> optionalLiterature = literatureRepository.findByUnid(literature.getUnid());
        if (optionalLiterature.isPresent()) {
            Literature lt = optionalLiterature.get();
            // 最近15天是否求助过相同的文献
            helpRecordRepository
                    .findByHelperEmailAndLiteratureId(helpRecord.getHelperEmail(), lt.getId())
                    .ifPresent(h -> {
                        throw new RepeatHelpRequestException();
                    });
            BeanUtil.copyProperties(literature, lt, CopyOptions.create().setIgnoreNullValue(true));
            literature = lt;
        }
        Literature literatureEntity = literatureRepository.save(literature);
        helpRecord.setLiteratureId(literatureEntity.getId());
        helpRecord = helpRecordRepository.save(helpRecord);
        asyncService.autoGive(helpRecord.getId());
    }

    @Override
    public void checkIsRepeat(String docTitle, String docHref, String email) {
        Literature literature = new Literature();
        literature.setDocTitle(docTitle).setDocHref(docHref);
        literature.createUnid();
        Optional<Literature> optionalLiterature = literatureRepository.findByUnid(literature.getUnid());
        if (optionalLiterature.isPresent()) {
            Literature lt = optionalLiterature.get();
            // 最近15天是否求助过相同的文献
            helpRecordRepository
                    .findByHelperEmailAndLiteratureId(email, lt.getId())
                    .ifPresent(h -> {
                        throw new RepeatHelpRequestException();
                    });
        }
    }
}
