package com.tgy.rtls.data.mapper.ES;

import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.ES
 * @Author: wuwei
 * @CreateTime: 2023-10-16 18:44
 * @Description: TODO
 * @Version: 1.0
 */
public interface EsTestMapper extends ElasticsearchRepository<ESInfraredOriginal, String> {


}
