package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author: He Zhigang
 * @Date: 2019/10/30 17:45
 * @Description:
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicInsert
@Table(name = "handler_log")
public class HandlerLog extends AbstractEntity {

    private String handlerName;

    private Integer beforeStatus;

    private Integer afterStatus;


}
