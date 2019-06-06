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

    @Query(value = "select * from literature_plan t1,\n" +
            "(select start_time, end_time from literature_plan t \n" +
            "where start_time > now() GROUP BY start_time, end_time \n" +
            "ORDER BY start_time limit 1) t2 where \n" +
            "DATE_FORMAT(t1.start_time,'%Y-%m-%d') = DATE_FORMAT(t2.start_time,'%Y-%m-%d') order by t1.end_time,t1.order_list", nativeQuery = true)
    List<LiteraturePlan> findByDate();


}
