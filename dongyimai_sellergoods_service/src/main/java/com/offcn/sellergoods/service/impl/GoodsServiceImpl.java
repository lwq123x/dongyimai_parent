package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional  //注解式的事务配置
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Goods goods) {
        //设置商品的审核状态,未审核
        goods.getGoods().setAuditStatus("0");
        //保存商品信息表 SPU
        goodsMapper.insert(goods.getGoods());
        //int i = 10/0;
        //取得商品信息表的主键 向商品扩展表设置商品主键
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //保存商品信息扩展表
        goodsDescMapper.insert(goods.getGoodsDesc());
        // 保存商品详情信息  SKU
        this.createItem(goods);

    }

    private void setItemValue(Goods goods, TbItem item) {
        item.setCategoryid(goods.getGoods().getCategory3Id()); //分类Id,三级分类
        item.setCreateTime(new Date()); //创建时间
        item.setUpdateTime(new Date()); //更新时间
        item.setGoodsId(goods.getGoods().getId()); //SPU的Id
        item.setSellerId(goods.getGoods().getSellerId()); //商家Id
        //根据分类Id查询分类对象
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName()); //分类名称
        //查询品牌信息
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(tbBrand.getName()); //品牌名称
        //查询商家信息
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(item.getSellerId());
        item.setSeller(tbSeller.getNickName()); //商家店铺名称

        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            String url = (String) imageList.get(0).get("url");
            item.setImage(url);
        }

    }

    private void createItem(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            if (!CollectionUtils.isEmpty(goods.getItemList())) {
                for (TbItem item : goods.getItemList()) {
                    //SKU名称  商品名称+规格选项
                    String title = goods.getGoods().getGoodsName();
                    Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                    for (String key : map.keySet()) {
                        title += map.get(key) + " ";
                    }

                    item.setTitle(title); //设置回SKU的名称
                    this.setItemValue(goods, item);
                    itemMapper.insert(item);
                }

            }
        } else {
            //不启用规格时,SKU信息需要手动设置
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName()); //商品名称
            item.setPrice(goods.getGoods().getPrice()); //商品价格
            item.setNum(9999);  //默认库存
            item.setStatus("1");
            item.setIsDefault("1");
            item.setSpec("{}");
            this.setItemValue(goods, item);
            itemMapper.insert(item);
        }
    }


    /**
     * 增加
     */
	/*@Override
	public void add(TbGoods goods) {
		goodsMapper.insert(goods);		
	}
*/

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //1.将商品的审核状态重新设置为 未审核
        goods.getGoods().setAuditStatus("0");
        //2.修改商品信息SPU
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //3.修改商品扩展信息
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //4.根据商品Id先将SKU列表信息删除
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(tbItemExample);
        //5.重新添加SKU信息
        this.createItem(goods);

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        //1.根据Id查询SPU商品信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        //2.根据Id查询商品扩展信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //3.根据商品Id查询商品SKU商品详情信息
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        //设置Id为查询条件
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
        //4.设置回复合实体对象中
        Goods goods = new Goods();
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //物理删除
            //goodsMapper.deleteByPrimaryKey(id);
            //逻辑删除
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");  //1表示已删除
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
        //修改商品SKU状态为禁用
        List<TbItem> itemList = this.findItemListByGoodsIdAndStatus(ids,"1");
        for (TbItem item : itemList) {
            item.setStatus("3"); //禁用
            itemMapper.updateByPrimaryKey(item);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            /*if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }*/
            criteria.andIsDeleteIsNull();//查询未删除记录
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 批量审核商品
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //1.遍历Id的集合
        for (Long id : ids) {
            //2.根据Id取得商品信息
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //3.设置审核状态
            tbGoods.setAuditStatus(status);
            //4.执行修改 更新商品信息到数据库
            goodsMapper.updateByPrimaryKey(tbGoods);
            //5.根据Id取得SKU列表
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
            for (TbItem item : itemList) {
                //6.设置SKU的审核状态
                item.setStatus(status);
                //7.执行修改SKU
                itemMapper.updateByPrimaryKey(item);
            }

        }

    }

    /**
     * 增量查找审核通过的SKU列表
     * @param ids
     * @param status
     * @return
     */
    @Override
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] ids, String status) {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo(status);
        criteria.andGoodsIdIn(Arrays.asList(ids));

        return itemMapper.selectByExample(tbItemExample);
    }

}
