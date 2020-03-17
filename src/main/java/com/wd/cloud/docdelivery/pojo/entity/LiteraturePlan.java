package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "literature_plan")
public class LiteraturePlan extends AbstractEntity {

    /**
     * 被排班人
     */
    @Column(name = "username")
    private String username;

    /**
     * 安排人
     */
    @Column(name = "arranger")
    private String arranger;

    @Column(name = "start_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @Column(name = "end_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    @Column(name = "order_list")
    private Integer orderList;

    @Column(name = "weight", columnDefinition = "double(4,2) default 1.00 COMMENT '权重'")
    private Integer weight;
}
