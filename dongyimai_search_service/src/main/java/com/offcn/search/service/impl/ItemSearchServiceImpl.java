package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.创建查询对象
        /*Query query = new SimpleQuery();
        //2.创建查询条件选择器, 并设置查询条件   is 对域进行分词查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //3.将选择器放回查询对象中
        query.addCriteria(criteria);
        //4.执行查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        //5.取得查询结果集
        List<TbItem> itemList = page.getContent();*/

        //处理搜索关键字,去除空格
        if (StringUtils.isNotEmpty((String)searchMap.get("keywords"))&&((String)searchMap.get("keywords")).indexOf(" ")>-1){
           String keywords = (String)searchMap.get("keywords");
           keywords = keywords.replace(" ","");  //去除空格
           searchMap.put("keywords",keywords);
        }

        //1.查询列表
        resultMap.putAll(this.searchList(searchMap));

        //2.根据关键字查询商品分类
        List<String> categoryList = this.findCategoryList(searchMap);
        resultMap.put("categoryList", categoryList);

        //如果分类查询条件有值,则需要根据分类进行检索品牌和规格,否则默认使用第一个分类的查询条件
        if (StringUtils.isNotEmpty((String) searchMap.get("category"))) {
            resultMap.putAll(this.findBrandAndSpecList((String) searchMap.get("category")));
        } else {
            if (categoryList.size() > 0) {
                resultMap.putAll(this.findBrandAndSpecList(categoryList.get(0)));
            }
        }

        //resultMap.put("rows", itemList);

        return resultMap;
    }

    /**
     * 导入SKU数据
     * @param itemList
     */
    @Override
    public void importItem(List<TbItem> itemList) {
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

    //删除SKU数据
    @Override
    public void deleteByGoodsId(List goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("solr删除成功!"+goodsIdList);
    }


    private Map<String, Object> searchList(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //1.1.创建一个支持高亮查询器对象
        HighlightQuery query = new SimpleHighlightQuery();
        //1.2.设置需要高亮显示的字段
        HighlightOptions options = new HighlightOptions();
        options.addField("item_title");
        //1.3.设置高亮显示的属性  前缀 后缀
        options.setSimplePrefix("<em style='color:red'>");
        options.setSimplePostfix("</em>");
        //1.4.关联高亮选项到高亮查询器对象
        query.setHighlightOptions(options);
        //1.5.设置查询条件 根据关键字查询
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);

        //2.1根据分类筛选
        if (StringUtils.isNotEmpty((String) searchMap.get("category"))) {
            //设置查询条件
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //创建过滤查询的对象
            FilterQuery filterQuery = new SimpleFilterQuery().addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //2.2根据品牌筛选
        if (StringUtils.isNotEmpty((String) searchMap.get("brand"))) {
            //设置查询条件
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //创建过滤查询的对象
            FilterQuery filterQuery = new SimpleFilterQuery().addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //2.3根据规格筛选
        if (null != searchMap.get("spec")) {
            //取得规格对象
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            //根据规格对象,取得key的列表
            for (String key : specMap.keySet()) {
                //根据key 进行拼音的转换
                //执行过滤查询
                Criteria filterCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }
        //2.4价格筛选  比较查询
        if (StringUtils.isNotEmpty((String) searchMap.get("price"))) {
            //根据 - 进行字符串拆分  500-1000  str[0]=500  str[1]=1000
            String[] str = ((String) searchMap.get("price")).split("-");
            if (!str[0].equals("0")) {//如果区间起点不为0
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(str[0]);
                FilterQuery filterQuery = new SimpleFilterQuery().addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!str[1].equals("*")) {//如果区间终点不等于*
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(str[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);

            }
        }
        //2.5 执行分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");   //当前页码
        if (null == pageNo) {
            pageNo = 1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (null == pageSize) {
            pageSize = 20;//默认20条
        }
        query.setOffset((pageNo-1)*pageSize);                //查询的起始记录数
        query.setRows(pageSize);  //要查询的记录数

        //2.6排序查询
        String sortValue = (String) searchMap.get("sort");  //排序规则
        String sortField = (String) searchMap.get("sortField"); //排序字段

        if (StringUtils.isNotEmpty(sortValue)){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }


        //3.发出带高亮数据查询请求
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //4.获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightList = highlightPage.getHighlighted();
        //5.遍历高亮集合
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlightList) {
            TbItem tbItem = tbItemHighlightEntry.getEntity();
            //注意:一定要对高亮显示的片段做判空操作   getSnipplets 是片段的意思
            if (tbItemHighlightEntry.getHighlights().size() > 0 && tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                //得到高亮显示的关键字
                List<HighlightEntry.Highlight> highlightEntries = tbItemHighlightEntry.getHighlights();
                //高亮结果结合
                List<String> snippletList = highlightEntries.get(0).getSnipplets();
                //重新设置回SKU对象
                tbItem.setTitle(snippletList.get(0));
            }
        }
        //6.把高亮数据集合放回Map
        resultMap.put("rows", highlightPage.getContent());
        resultMap.put("total",highlightPage.getTotalElements());//总记录数
        resultMap.put("totalPages",highlightPage.getTotalPages()); //总页数
        return resultMap;
    }


    /**
     * 查询分类列表
     *
     * @param searchMap
     * @return
     */
    private List<String> findCategoryList(Map<String, Object> searchMap) {
        List<String> categoryList = new ArrayList<String>();
        //1.创建查询对象
        Query query = new SimpleQuery();
        //2.设置分组字段
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //3.创建对应的查询条件,按照关键字进行查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //4.执行分组查询,得到分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //5.根据列得到分组结果集入口
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //6.得到分组结果入口页
        Page<GroupEntry<TbItem>> page = groupResult.getGroupEntries();
        //7.得到分组入口集合
        List<GroupEntry<TbItem>> groupEntryList = page.getContent();
        for (GroupEntry<TbItem> groupEntry : groupEntryList) {
            //8.取得分组的结果并放入到重新定义的集合中||将分组结果的名称封装到返回值中
            categoryList.add(groupEntry.getGroupValue());
        }
        return categoryList;

    }

    /**
     * 查询品牌和规格列表
     *
     * @param category
     * @return
     */
    private Map<String, Object> findBrandAndSpecList(String category) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //1.通过分类名称在缓存中查询模板Id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (null != typeId) {
            //2.通过模板Id在缓存中查询品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            resultMap.put("brandList", brandList);
            //3.通过模板Id在缓存中查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            resultMap.put("specList", specList);
        }
        return resultMap;
    }
}
