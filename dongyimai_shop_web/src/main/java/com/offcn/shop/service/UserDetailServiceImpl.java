package com.offcn.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义认证类
 */
public class UserDetailServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {

        //1.获得权限列表
        //接口
        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
        //new一个具体的实现
        authorityList.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //2.配置验证
        //根据sellerId查询商家对象
        TbSeller tbSeller = sellerService.findOne(sellerId);
        if (null != tbSeller) {
            //判断审核状态  审核通过的
            if (tbSeller.getStatus().equals("1")) {
                return new User(sellerId, tbSeller.getPassword(), authorityList);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
