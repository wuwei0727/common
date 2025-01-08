package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.pay.Product;
import com.tgy.rtls.data.mapper.pay.ProductMapper;
import com.tgy.rtls.data.service.park.ProductService;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park.impl
 * @Author: wuwei
 * @CreateTime: 2023-11-15 16:08
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}