package com.tgy.rtls.data.service.es;

import com.tgy.rtls.data.entity.es.ESMag;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.ES
 * @Author: wuwei
 * @CreateTime: 2023-10-16 17:28
 * @Description: TODO
 * @Version: 1.0
 */
public interface ESMagService extends ElasticsearchRepository<ESMag, Integer> {
}
