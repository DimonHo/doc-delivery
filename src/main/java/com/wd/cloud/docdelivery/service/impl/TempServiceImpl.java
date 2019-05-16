package com.wd.cloud.docdelivery.service.impl;

import com.wd.cloud.docdelivery.pojo.entity.DocFile;
import com.wd.cloud.docdelivery.pojo.entity.GiveRecord;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import com.wd.cloud.docdelivery.repository.DocFileRepository;
import com.wd.cloud.docdelivery.repository.GiveRecordRepository;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.service.TempService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author He Zhigang
 * @date 2019/1/2
 * @Description:
 */
@Slf4j
@Service("tempService")
public class TempServiceImpl implements TempService {

    @Autowired
    LiteratureRepository literatureRepository;

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    DocFileRepository docFileRepository;


    @Override
    public int updateLiteratureUnid() {
        List<Literature> literatures = literatureRepository.findByUnidIsNull();
        for (Literature literature : literatures) {
            literature.updateUnid();
            try {
                literatureRepository.save(literature);
            } catch (Exception e) {
                log.error("唯一主键冲突:[{}]", literature.getUnid());
            }

        }
        return literatures.size();
    }

    @Override
    public int deleteLiteratureUnid() {

        //查询所有为null的数据
        List<Map<String, String>> literatures = literatureRepository.findByUnidIsNullGroupBy();
        //遍历处所有null的数据得到它的doc_href与doc_title
        for (Map literature : literatures) {
            update(literature);
        }
        return 0;
    }

    @Override
    public void updateHandlerName() {
        literatureRepository.findAll().forEach(literature -> {
            docFileRepository.findByLiteratureIdOrderByReusingDescGmtModifiedDesc(literature.getId()).stream().findFirst().ifPresent(docFile -> {
                literature.setLastHandlerName(docFile.getHandlerName());
                literatureRepository.save(literature);
            });
        });
    }

    @Transactional
    void update(Map literature) {
        //根据doc_href与doc_title查询数据。
        String docHref = literature.get("doc_href") == null ? null : literature.get("doc_href").toString();
        String docTitle = literature.get("doc_title").toString();
        log.info("[{}],[{}]", docHref, docTitle);
        List<Literature> byDocTitleAndDocHref = docHref == null ? literatureRepository.findByDocHrefIsNullAndDocTitle(docTitle) : literatureRepository.findByDocHrefAndDocTitle(docHref, docTitle);
        Literature unidLiterature = null;
        List<Long> ids = new ArrayList<>();

        List<Literature> idsi = new ArrayList<>();
        //遍历得到重复数据
        for (Literature literature1 : byDocTitleAndDocHref) {
            //遍历数据得到uuid不为NULL的数据
            if (literature1.getUnid() != null) {
                unidLiterature = literature1;
            } else {
                ids.add(literature1.getId());
                idsi.add(literature1);
            }
        }
        //遍历所有的数据根据ID到help_record表与doc_file找到对应的ID
        List<HelpRecord> byLiteratureId = helpRecordRepository.findByLiteratureIdIn(ids);
        for (HelpRecord helpRecord : byLiteratureId) {
            helpRecord.setLiteratureId(unidLiterature.getId());
            //修改表中的id
            try {
                helpRecordRepository.save(helpRecord);
            } catch (Exception e) {
                helpRecordRepository.delete(helpRecord);
                giveRecordRepository.deleteByHelpRecordId(helpRecord.getId());

            }
        }

        List<DocFile> byLiteratureId1 = docFileRepository.findByLiteratureIdIn(idsi);
        for (DocFile docFile : byLiteratureId1) {
            docFile.setLiteratureId(unidLiterature.getId());
            try {
                docFileRepository.save(docFile);
            } catch (Exception e) {
                DocFile byFileIdAndLiterature = docFileRepository.findByFileIdAndLiteratureId(docFile.getFileId(), docFile.getLiteratureId()).orElse(new DocFile());
                List<GiveRecord> byDocFileId = giveRecordRepository.findByFileId(docFile.getFileId());
                for (GiveRecord giveRecord : byDocFileId) {
                    giveRecord.setFileId(byFileIdAndLiterature.getFileId());
                }
                giveRecordRepository.saveAll(byDocFileId);
                docFileRepository.deleteById(docFile.getId());

            }

        }
    }


}
