package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//1.新增规格名称
		specificationMapper.insert(specification.getSpecification());
		//2.得到规格名称的对象的主键Id

		if (null!=specification.getSpecificationOptionList()&&specification.getSpecificationOptionList().size()>0){
			 for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()){
				 //3.向规格选项对象中设置外键
				 specificationOption.setSpecId(specification.getSpecification().getId());
				 //4.新增规格选项
				 specificationOptionMapper.insert(specificationOption);

			 }
		}


	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//1.修改规格名称对象
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		//2.根据规格ID删除原有的规格选项集合
		TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());
		//执行删除
		specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
		//3.重新添加规格选项集合
		if (null!=specification.getSpecificationOptionList()&&specification.getSpecificationOptionList().size()>0){
			for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()){
				//3.向规格选项对象中设置外键
				specificationOption.setSpecId(specification.getSpecification().getId());
				//4.新增规格选项
				specificationOptionMapper.insert(specificationOption);

			}
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//1.查询规格名称
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//2.根据规格名称ID查询规格选项列表
		TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
		//3.设置查询条件为ID外键
		criteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
		//4.设置复合实体类
		Specification specification = new Specification();
		specification.setSpecification(tbSpecification);
		specification.setSpecificationOptionList(specificationOptionList);
		return specification;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
			//关联删除规格选项
			//2.根据规格ID删除规格选项集合
			TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
			criteria.andSpecIdEqualTo(id);
			//执行删除
			specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 查询规格的下拉列表
	 * @return
	 */
	@Override
	public List<Map> selectOptions() {
		return specificationMapper.selectOptions();
	}

}
