package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author York
 * @create 2018-11-06 18:19
 */
public class SkuAttrValue implements Serializable {
    /**
     * 主键id
     */
    @Id
    @Column
    String id;

    /**
     * 平台属性id
     */
    @Column
    String attrId;

    /**
     * 平台属性值id
     */
    @Column
    String valueId;

    /**
     * 商品id
     */
    @Column
    String skuId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }
}
