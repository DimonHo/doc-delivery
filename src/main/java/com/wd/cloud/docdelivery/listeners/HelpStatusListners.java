package com.wd.cloud.docdelivery.listeners;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.wd.cloud.docdelivery.pojo.entity.HandlerLog;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.HandlerLogRepository;
import com.wd.cloud.docdelivery.repository.VHelpRecordRepository;
import com.wd.cloud.docdelivery.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2019/1/30
 * @Description: 数据监听器，监听helpRecord.status字段状态变化，发送邮件
 */
@Slf4j
@Component
public class HelpStatusListners extends DefaultLoadEventListener implements PostCommitUpdateEventListener {

    /**
     * 3:求助第三方，4：求助成功，5：疑难文献
     */
    private static final List<Integer> SEND_STATUS = CollectionUtil.newArrayList(3, 4);
    @Autowired
    MailService mailService;

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;

    @Autowired
    HandlerLogRepository handlerLogRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void registerListeners() {
        log.info("register listeners ******");
        SessionFactoryImpl sessionFactoryImpl = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry eventListenerRegistry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
        eventListenerRegistry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(this);
    }

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent postUpdateEvent) {
        log.warn("[" + postUpdateEvent.getPersister().getEntityName() + "] commit 失败");
    }

    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        log.info("update start***********");
        if (postUpdateEvent.getEntity() instanceof HelpRecord) {
            HelpRecord helpRecord = (HelpRecord) postUpdateEvent.getEntity();
            boolean statusChangeSend = false;
            boolean difficultChangeSend = false;
            for (int i = 0; i < postUpdateEvent.getPersister().getPropertyNames().length; i++) {

                // 是否是status字段
                boolean isColumnEqStatus = "status".equals(postUpdateEvent.getPersister().getPropertyNames()[i]);
                if (isColumnEqStatus){
                    // 新旧字段值是否不一样
                    boolean newNotEqOld = postUpdateEvent.getOldState()[i] != postUpdateEvent.getState()[i];
                    if (newNotEqOld){
                        HandlerLog handlerLog = new HandlerLog();
                        handlerLog.setHelpRecordId(helpRecord.getId())
                                .setHandlerName(helpRecord.getHandlerName())
                                .setBeforeStatus((Integer) postUpdateEvent.getOldState()[i])
                                .setAfterStatus((Integer) postUpdateEvent.getState()[i]);
                        handlerLogRepository.save(handlerLog);
                    }
                    // 新值是否在status列表中
                    boolean newStatusContains = SEND_STATUS.contains(postUpdateEvent.getState()[i]);
                    statusChangeSend = newNotEqOld && newStatusContains;
                    continue;
                }

                // 是否是difficult字段
                boolean isColumnEqDifficult = "difficult".equals(postUpdateEvent.getPersister().getPropertyNames()[i]);
                // 新值difficult是否是从false转换为true
                if (isColumnEqDifficult){
                    difficultChangeSend = BooleanUtil.isFalse((boolean)postUpdateEvent.getOldState()[i])
                            && BooleanUtil.isTrue((boolean)postUpdateEvent.getState()[i]);
                }
            }
            if (statusChangeSend || difficultChangeSend) {
                Optional<VHelpRecord> optionalVHelpRecord = vHelpRecordRepository.findById(helpRecord.getId());
                optionalVHelpRecord.ifPresent(vHelpRecord -> mailService.sendMail(vHelpRecord));
            }
        }
    }

    /**
     * @param entityPersister
     * @deprecated
     */
    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return false;
    }
}
