package com.offcn.page.service.impl;

//import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Service
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Value("${file_url}")
    private String FILE_URL;

    /**
     * 生成商品详情页的静态页面
     *
     * @param goodsId
     * @return
     */
    @Override
    public boolean genItemPage(Long goodsId) {
        try {
            //1.获得配置对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //2.获得freemarker的模板对象
            Template template = configuration.getTemplate("item.ftl");
            //3.查询SPU 商品信息表
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            //4.查询商品扩展表数据
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //5.查询SKU 商品详情信息
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andStatusEqualTo("1");  //审核通过的
            criteria.andGoodsIdEqualTo(goodsId); //指定SPU的Id
            tbItemExample.setOrderByClause("is_default desc"); //根据是否默认降序排序,保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);


            //查询商品分类信息
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());

            Map<String, Object> dataSource = new HashMap<String, Object>();
            dataSource.put("goods", tbGoods);
            dataSource.put("goodsDesc", tbGoodsDesc);
            dataSource.put("itemCat1",itemCat1);
            dataSource.put("itemCat2",itemCat2);
            dataSource.put("itemCat3",itemCat3);
            dataSource.put("itemList",itemList);

            //6.生成静态页面
            FileWriter out = new FileWriter(new File(FILE_URL + goodsId + ".html"));
            template.process(dataSource, out);
            //7.关闭文件流
            out.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除商品详情页
     * @param ids
     * @return
     */
    @Override
    public boolean deleteItemHtml(Long[] ids) {
        try {
            for (Long id : ids) {
                new File(FILE_URL+id+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
