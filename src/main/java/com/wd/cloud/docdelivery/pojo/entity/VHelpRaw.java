package com.wd.cloud.docdelivery.pojo.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Hu Langshi
 * @date 2019/10/14
 * @Description:
 */
@Data
@Accessors(chain = true)
@Immutable
@Entity
@Table(name = "v_help_raw")
public class VHelpRaw{
    @Id
    private Long id;

    private Date gmtCreate;

    private Date gmtModified;

    private String info;

    private Long helpRecordId;

    private Integer invalid;

    private String helperEmail;

    private String helperIp;

    private String helperName;

    private Long helpChannel;

    private String orgFlag;

    private String orgName;

    private String channelName;

    private String channelUrl;

    private String channelTemplate;

    private String bccs;

    private Long exp;

    @Column(name = "is_anonymous")
    private Boolean anonymous;

    @Column(name = "is_send")
    private Boolean send;

    private Long literatureId;

    private Integer status;

    private String remark;

    private String docTitle;

    private String docHref;

    private String author;

    private String doi;

    private String summary;

    private String unid;

    private String year;

    private String watchName;

    @Column(name = "is_difficult")
    private Boolean difficult;

    private String handlerName;

    private Integer giveType;

    private String giverName;

}
