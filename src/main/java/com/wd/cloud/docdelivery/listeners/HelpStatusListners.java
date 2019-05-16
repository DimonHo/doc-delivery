package com.wd.cloud.docdelivery.listeners;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.wd.cloud.docdelivery.feign.PdfSearchServerApi;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.MailService;
import com.wd.cloud.docdelivery.task.AutoGiveTask;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2019/1/30
 * @Description: 数据监听器，监听helpRecord.status字段状态变化，发送邮件
 */
@Slf4j
@Component
public class HelpStatusListners extends DefaultLoadEventListener implements PostUpdateEventListener, PostInsertEventListener {
    /**
     * HelpRecord.status字段名称
     */
    private static final String HELP_RECORD_STATUS = "status";
    /**
     * 3:求助第三方，4：求助成功，5：疑难文献
     */
    private static final List<Integer> SEND_STATUS = CollectionUtil.newArrayList(3, 4, 5);
    @Autowired
    MailService mailService;
    @Autowired
    VHelpRecordRepository vHelpRecordRepository;
    @Autowired
    HelpRecordRepository helpRecordRepository;
    @Autowired
    GiveRecordRepository giveRecordRepository;
    @Autowired
    LiteratureRepository literatureRepository;
    @Autowired
    DocFileRepository docFileRepository;
    @Autowired
    PdfSearchServerApi pdfSearchServerApi;

    @Autowired
    ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void registerListeners() {
        log.info("register listeners ******");
        SessionFactoryImpl sessionFactoryImpl = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry eventListenerRegistry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
        eventListenerRegistry.getEventListenerGroup(EventType.POST_INSERT).appendListener(this);
        eventListenerRegistry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(this);
    }


    /**
     * 插入后，发送通知邮件或自动应助成功邮件
     *
     * @param postInsertEvent
     */
    @Override
    public void onPostInsert(PostInsertEvent postInsertEvent) {
        log.info("insert start***********");
        log.debug(postInsertEvent.getEntity() + " inserted");

        if (postInsertEvent.getEntity() instanceof HelpRecord) {
            HelpRecord helpRecord = (HelpRecord) postInsertEvent.getEntity();

//            Optional<VHelpRecord> optionalVHelpRecord = vHelpRecordRepository.findById(helpRecord.getId());
//            optionalVHelpRecord.ifPresent(vHelpRecord -> mailService.sendMail(vHelpRecord));

            // 3秒钟后执行自动应助
            threadPoolTaskScheduler.schedule(new AutoGiveTask(helpRecord.getId()), DateUtil.offsetSecond(new Date(), 3));

        }

    }

    /**
     * 更新后，发送给用户应助结果邮件
     *
     * @param postUpdateEvent
     */
    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        log.info("update start***********");
        if (postUpdateEvent.getEntity() instanceof HelpRecord) {
            HelpRecord helpRecord = (HelpRecord) postUpdateEvent.getEntity();
            for (int i = 0; i < postUpdateEvent.getPersister().getPropertyNames().length; i++) {
                if (HELP_RECORD_STATUS.equals(postUpdateEvent.getPersister().getPropertyNames()[i])
                        && postUpdateEvent.getOldState()[i] != postUpdateEvent.getState()[i]
                        && SEND_STATUS.contains(postUpdateEvent.getState()[i])) {
                    Optional<VHelpRecord> optionalVHelpRecord = vHelpRecordRepository.findById(helpRecord.getId());
                    optionalVHelpRecord.ifPresent(vHelpRecord -> mailService.sendMail(vHelpRecord));

                }
            }
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return false;
    }

}
