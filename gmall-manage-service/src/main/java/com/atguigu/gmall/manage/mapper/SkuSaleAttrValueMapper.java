package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author York
 * @create 2018-11-06 21:20
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    public List<SkuSaleAttrValue>selectSkuSaleAttrValueListBySpu(String spuId);
}
