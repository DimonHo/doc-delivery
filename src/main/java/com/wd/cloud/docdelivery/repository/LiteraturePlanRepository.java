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
            "where start_time > now()  ORDER BY start_time limit 1) t2 \n" +
            "where \n" +
            "DATE_FORMAT(t1.start_time,'%Y-%m-%d') = DATE_FORMAT(t2.start_time,'%Y-%m-%d') \n" +
            "order by t1.end_time,t1.order_list", nativeQuery = true)
    List<LiteraturePlan> findNextLiteraturePlans();

    @Query(value = "select * from literature_plan where DATE_FORMAT(start_time,'%Y-%m-%d') = DATE_FORMAT(now(),'%Y-%m-%d') order by start_time,order_list", nativeQuery = true)
    List<LiteraturePlan> findNowDayLiteraturePlans();

    /**
     * 查询当前时间区间内的排班人列表
     * @return
     */
    @Query(value = "select * from literature_plan t where start_time <= now() and end_time >= now() order by start_time ,order_list", nativeQuery = true)
    List<LiteraturePlan> findByNowPlans();


    /**
     * 查询最接近当前时间的下一个排班人列表
     * @return
     */
    @Query(value = "select * from literature_plan where start_time = (select start_time from literature_plan where start_time > now() GROUP BY start_time order by start_time asc limit 1)", nativeQuery = true)
    List<LiteraturePlan> findByNextPlans();

}
