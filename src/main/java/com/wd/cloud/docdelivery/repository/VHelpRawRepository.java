package com.wd.cloud.docdelivery.repository;

import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @author Hu Langshi
 * @date 2019/10/24
 * @Description:
 */
public interface VHelpRawRepository extends JpaRepository<VHelpRaw, Long>, JpaSpecificationExecutor<VHelpRaw> {

    List<VHelpRaw> findByHelperNameAndStatus(String helperName, Integer status);

    @Modifying
    @Transactional
    @Query(value = "select * from v_help_raw where if(?1 != '',gmt_create = ?1,1=1) and if(?2 is not null ,is_anonymous = ?2,1=1) and if(?3 != '',help_channel = ?3,1=1) " +
            "and if(?4 != '',helper_email = ?4,1=1) and if(?5 != '',helper_ip = ?5,1=1) and if(?6 != '',helper_name = ?6,1=1) and if(?7 != '',org_flag = ?7,1=1)" +
            "and if(?8 != '',org_name = ?8,1=1) and if(?9 is not null,help_record_id = ?9,1=1) and if(?10 is not null,invalid = ?10,1=1)", nativeQuery = true)
    List<VHelpRaw> findHelpRaw(Date gmtCreate, Boolean anonymous, Long helpChannel, String helperEmail, String helperIp, String helperName, String orgFlag, String orgName, Long helpRecordId, Integer invalid);

}
