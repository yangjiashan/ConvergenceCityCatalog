package com.fgi.city.dao;

import com.fgi.city.entity.SecretKeyBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SecretKeyMapper {
    @Select("select * from SECRETKEYTABLE where orgid = #{orgId} and btype = '1' and padding = '1' and ktype = '1'")
    SecretKeyBean querySM4KeyByOrgId(@Param("orgId") String orgId); // 根据机构ID获取SM4秘钥

    @Select("select * from SECRETKEYTABLE where orgid = #{orgId} and btype = '1' and ktype = '2'")
    SecretKeyBean querySM2KeyByOrgId(@Param("orgId") String orgId); // 根据机构ID获取SM2公钥
}
