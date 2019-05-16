package com.wd.cloud.docdelivery.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author He Zhigang
 * @date 2018/12/26
 * @Description:
 */
@Data
@Accessors(chain = true)
public class LiteratureDTO {
    List<DocFileDTO> docFiles;
    private Long id;
    private String docHref;
    private String docTitle;
    private String author;
    private String year;
    private String doi;
    private String summary;
    private boolean reusing;
    private String lastHandlerName;
}
