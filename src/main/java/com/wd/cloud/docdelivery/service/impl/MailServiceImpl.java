package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.util.StrUtil;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.MailServerApi;
import com.wd.cloud.docdelivery.model.MailTemplateModel;
import com.wd.cloud.docdelivery.pojo.dto.MailMessage;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.service.MailService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2018/5/17
 * @Description:
 */
@Slf4j
@Service("mailService")
public class MailServiceImpl implements MailService {

    @Autowired
    Global global;
    @Autowired
    HelpRecordRepository helpRecordRepository;
    @Autowired
    MailServerApi mailServerApi;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Async
    @Override
    public void sendMail(VHelpRecord vHelpRecord) {

        MailTemplateModel mailTemplateModel = buildMailTemplateModel(vHelpRecord);
        String mailTitle = mailTemplateModel.getMailTitle();
        String mailContent = buildContent(mailTemplateModel);
        Optional<HelpRecord> optionalHelpRecord = helpRecordRepository.findById(vHelpRecord.getId());
        if (optionalHelpRecord.isPresent()) {
            HelpRecord helpRecord = optionalHelpRecord.get();
            MailMessage mailMessage = new MailMessage().setTitle(mailTitle).setContent(mailContent).setHtml(true).setBccs(vHelpRecord.getBccs());
            mailMessage.setTos(helpRecord.getHelperEmail());
            // 防止重复发送
            String businessId = helpRecord.getId() + "-" + helpRecord.getStatus() + "-" + helpRecord.getDifficult();
            try{
                ResponseModel responseModel = mailServerApi.send(helpRecord.getStatus() == 4 ? global.getBizSuccess() : global.getBizOther(), businessId, mailMessage);
                if (responseModel.isError()) {
                    if (responseModel.getStatus() == 403){
                        helpRecord.setSend(true);
                        log.warn("重复发送的邮件:{}", businessId);
                    }else{
                        helpRecord.setSend(false);
                        log.error(responseModel.getMessage());
                    }
                } else {
                    helpRecord.setSend(true);
                }
            }catch (Exception e){
                helpRecord.setSend(false);
                log.error("邮件服务调用失败",e);
            }
            helpRecordRepository.save(helpRecord);
        }
    }

    /**
     * 构建邮件内容模板
     *
     * @param vHelpRecord
     * @return
     */
    private MailTemplateModel buildMailTemplateModel(VHelpRecord vHelpRecord) {
        MailTemplateModel mailTemplateModel = new MailTemplateModel();
        mailTemplateModel.setChannelName(vHelpRecord.getChannelName())
                .setChannelUrl(vHelpRecord.getChannelUrl())
                .setDocTitle(vHelpRecord.getDocTitle());
        Optional<HelpStatusEnum> optionalHelpStatusEnum = HelpStatusEnum.match(vHelpRecord.getStatus());
        if (optionalHelpStatusEnum.isPresent()) {
            switch (optionalHelpStatusEnum.get()) {
                case HELP_THIRD:
                    mailTemplateModel.setMailTitle(String.format("回复: [文献互助•疑难文献]-%s", vHelpRecord.getDocTitle()))
                            .setTemplate(String.format(vHelpRecord.getChannelTemplate(), vHelpRecord.getOrgName() + "-third"));
                    break;
                case HELP_SUCCESSED:
                    mailTemplateModel.setMailTitle(String.format("回复: [文献互助•成功]-%s", vHelpRecord.getDocTitle()))
                            .setDownloadUrl(buildDownloadUrl(vHelpRecord.getId()))
                            .setTemplate(String.format(vHelpRecord.getChannelTemplate(), vHelpRecord.getOrgName() + "-success"));
                    break;
                default:
                    mailTemplateModel.setMailTitle(String.format("回复: [文献互助•失败]-%s", vHelpRecord.getDocTitle()))
                            .setTemplate(String.format(vHelpRecord.getChannelTemplate(), vHelpRecord.getOrgName() + "-failed"));
            }
        }
        return mailTemplateModel;
    }


    /**
     * 构建邮件内容
     *
     * @return
     */
    private String buildContent(MailTemplateModel mailTemplateModel) {
        String templateFile = mailTemplateModel.getTemplate();
        String content = null;
        Template template;
        try {
            template = freeMarkerConfigurer.getConfiguration().getTemplate(templateFile);
            content = FreeMarkerTemplateUtils.processTemplateIntoString(template, mailTemplateModel);
        } catch (TemplateException | IOException e) {
            log.info("模板文件[{}]不存在，使用默认模板", templateFile);
            templateFile = templateFile.replace(StrUtil.subBefore(templateFile, "-", false), "default");
            try {
                template = freeMarkerConfigurer.getConfiguration().getTemplate(templateFile);
                content = FreeMarkerTemplateUtils.processTemplateIntoString(template, mailTemplateModel);
            } catch (TemplateException | IOException e1) {
                log.error("模板文件[" + templateFile + "]不存在!", e1);
            }
        }
        return content;
    }


    /**
     * 构建全文下载链接
     *
     * @param helpRecordId
     * @return
     */
    private String buildDownloadUrl(Long helpRecordId) {
        return global.getCloudHost() + "/doc-delivery/file/download/" + helpRecordId;
    }
}
