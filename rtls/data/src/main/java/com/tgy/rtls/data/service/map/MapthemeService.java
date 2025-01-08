package com.tgy.rtls.data.service.map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.map.Maptheme;

import java.util.List;
public interface MapthemeService extends IService<Maptheme>{


    int updateBatch(List<Maptheme> list);

    int batchInsert(List<Maptheme> list);

    int addMaptheme(Maptheme maptheme);

    int editMaptheme(Maptheme maptheme);

}
