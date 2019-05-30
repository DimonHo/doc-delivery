package com.wd.cloud.docdelivery.repository;

import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author He Zhigang
 * @date 2018/5/31
 * @Description:
 */
public interface LiteraturePlanRepository extends JpaRepository<LiteraturePlan, Long> {

    @Query(value = "select * from literature_plan where date_format(start_time,'%Y-%m-%d') = date_format(?1,'%Y-%m-%d') order by end_time,order_list", nativeQuery = true)
    List<LiteraturePlan> findByDate(Date time);


}
