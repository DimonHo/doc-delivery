package com.wd.cloud.docdelivery.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.wd.cloud.commons.util.StrUtil;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.pojo.dto.GiveRecordDTO;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.*;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * @Author: He Zhigang
 * @Date: 2019/10/14 14:38
 * @Description:
 */
public class BizUtil {

    /**
     * 匿名和邮箱隐藏处理
     *
     * @param vHelpRecord
     * @return
     */
    public static VHelpRecord anonymous(VHelpRecord vHelpRecord) {
        if (BooleanUtil.isTrue(vHelpRecord.getAnonymous())) {
            vHelpRecord.setHelperEmail("匿名").setHelperName("匿名");
        } else {
            String helperEmail = vHelpRecord.getHelperEmail();
            String s = StrUtil.hideMailAddr(helperEmail);
            vHelpRecord.setHelperEmail(s);
        }
        return vHelpRecord;
    }

    public static Page<HelpRecordDTO> coversHelpRecordDTO(Page<VHelpRecord> helpRecordPage) {
        return helpRecordPage.map(vHelpRecord -> {
            HelpRecordDTO helpRecordDTO = BeanUtil.toBean(anonymous(vHelpRecord), HelpRecordDTO.class);
            helpRecordDTO.setDocTitle(vHelpRecord.getDocTitle()).setDocHref(vHelpRecord.getDocHref());
            if (helpRecordDTO.getStatus() == HelpStatusEnum.HELP_SUCCESSING.value()){
                helpRecordDTO.setStatus(HelpStatusEnum.HELPING.value());
            }
            return helpRecordDTO;
        });
    }

    public static Page<GiveRecordDTO> coversGiveRecordDTO(Page<GiveRecord> giveRecordPage, LiteratureRepository literatureRepository, HelpRecordRepository helpRecordRepository) {
        return giveRecordPage.map(giveRecord -> {
            GiveRecordDTO giveRecordDTO = BeanUtil.toBean(giveRecord, GiveRecordDTO.class);
            Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(giveRecord.getHelpRecordId());
            optionalHelpRecord.ifPresent(helpRecord -> {
                giveRecordDTO.setHelperEmail(BooleanUtil.isTrue(helpRecord.getAnonymous()) ? "匿名" : StrUtil.hideMailAddr(helpRecord.getHelperEmail()))
                        .setRemark(helpRecord.getRemark()).setOrgName(helpRecord.getOrgName());
                Optional<Literature> optionalLiterature = literatureRepository.findById(helpRecord.getLiteratureId());
                optionalLiterature.ifPresent(literature -> {
                    giveRecordDTO.setDocTitle(literature.getDocTitle()).setDocHref(literature.getDocHref());
                });
            });
            return giveRecordDTO;
        });
    }


    public static Page<VHelpRaw> coversVhelpRaw(Page<VHelpRaw> vHelpRaws){
        return vHelpRaws.map(vHelpRaw -> {
            if (vHelpRaw.getStatus() != null){
                if (vHelpRaw.getStatus() == HelpStatusEnum.HELP_SUCCESSING.value()){
                    vHelpRaw.setStatus(HelpStatusEnum.HELPING.value());
                }
                return vHelpRaw;
            }else {
                return vHelpRaw;
            }
        });
    }

    public static int level(boolean isInside, boolean isLogin, boolean isVilified, boolean isTeacher, boolean isBuy){
        int level = 0;
        if (isInside){
            level += 1;
        }
        if (isLogin){
            level += 2;
        }
        if (isVilified){
            level += 4;
        }
        if (isTeacher){
            level += 8;
        }
        if (isBuy){
            level += 16;
        }
        return level;
    }
}
