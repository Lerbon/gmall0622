package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author York
 * @create 2018-11-06 18:22
 */
public class SkuSaleAttrValue implements Serializable {

    /**
     * 主键id
     */
    @Id
    @Column
    String id;

    /**
     * 商品id
     */
    @Column
    String skuId;

    /**
     * 销售属性id
     */
    @Column
    String saleAttrId;

    /**
     * 销售属性值id
     */
    @Column
    String saleAttrValueId;

    /**
     * 销售属性名
     */
    @Column
    String saleAttrName;

    /**
     * 销售属性值名称
     */
    @Column
    String saleAttrValueName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSaleAttrId() {
        return saleAttrId;
    }

    public void setSaleAttrId(String saleAttrId) {
        this.saleAttrId = saleAttrId;
    }

    public String getSaleAttrValueId() {
        return saleAttrValueId;
    }

    public void setSaleAttrValueId(String saleAttrValueId) {
        this.saleAttrValueId = saleAttrValueId;
    }

    public String getSaleAttrName() {
        return saleAttrName;
    }

    public void setSaleAttrName(String saleAttrName) {
        this.saleAttrName = saleAttrName;
    }

    public String getSaleAttrValueName() {
        return saleAttrValueName;
    }

    public void setSaleAttrValueName(String saleAttrValueName) {
        this.saleAttrValueName = saleAttrValueName;
    }
}
