package com.tgy.rtls.data.mapper.ES;

import com.tgy.rtls.data.entity.es.ESMag;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.ES
 * @Author: wuwei
 * @CreateTime: 2023-10-16 18:44
 * @Description: TODO
 * @Version: 1.0
 */
public interface EsMagMapper extends ElasticsearchRepository<ESMag, String> {


}
