package com.offcn.util;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入solr的工具类
 */
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItem() {
        //读取数据库，从数据库读取sku数据
        TbItemExample tbItemExample = new TbItemExample();
        //创建查询条件选择器
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo("1");  //审核通过的
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);

        for (TbItem item : itemList) {
            System.out.println(item.getTitle() + "=====" + item.getPrice());

            //取得规格属性 并做JSON类型转换
            Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            Map<String, String> pinyinMap = new HashMap<String, String>();
            for (String key : specMap.keySet()) {
                //完成拼音转换
                pinyinMap.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }
            //重新放回域中  solr数据重新更新,要先清空
            item.setSpecMap(pinyinMap);

        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("导入成功!");

    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItem();
    }
}
