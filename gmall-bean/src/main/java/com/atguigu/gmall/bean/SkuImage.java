package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author York
 * @create 2018-11-06 18:16
 */
public class SkuImage implements Serializable {

    /**
     * 主键id
     */
    @Id
    @Column
    String id;

    /**
     * 具体商品Id
     */
    @Column
    String skuId;

    /**
     * 图片名称
     */
    @Column
    String imgName;

    /**
     * 图片路径
     */
    @Column
    String imgUrl;

    /**
     * spu默认图片id
     */
    @Column
    String spuImgId;

    /**
     * 是否为默认图片
     */
    @Column
    String isDefault;

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

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSpuImgId() {
        return spuImgId;
    }

    public void setSpuImgId(String spuImgId) {
        this.spuImgId = spuImgId;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}
