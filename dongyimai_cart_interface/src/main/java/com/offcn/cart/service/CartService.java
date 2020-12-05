package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

/**
 * 购物车接口
 */
public interface CartService {

    /**
     * 添加SKU商品到购物车集合
     *
     * @param srcCartList  原购物车集合
     * @param itemId  SKU的ID
     * @param num  购买数量
     * @return 新的购物车集合
     */
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList, Long itemId, Integer num);

    /**
     * 从缓存中获得当前登录人的购物车列表
     * @param username  登录人的账号
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 根据当前登录人保存购物车集合到缓存
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList_cookie
     * @param cartList_redis
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList_cookie,List<Cart> cartList_redis);
}
