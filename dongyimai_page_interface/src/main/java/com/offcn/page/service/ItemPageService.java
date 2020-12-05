package com.offcn.page.service;

public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemPage(Long goodsId);

    /**
     * 删除商品详情页
     * @param ids
     * @return
     */
    public boolean deleteItemHtml(Long[] ids);
}
