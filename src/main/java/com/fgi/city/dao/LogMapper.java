package com.fgi.city.dao;

import com.fgi.city.entity.LogBean;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogMapper {

    @Insert("insert into cityreportlog(id,username,orgname,singleorgid,interfacename,accesstype,inputparams,outputparams,status) values(" +
            "#{logBean.id,jdbcType=VARCHAR}, #{logBean.username,jdbcType=VARCHAR}, #{logBean.orgname,jdbcType=VARCHAR}, " +
            "#{logBean.singleorgid,jdbcType=VARCHAR}, #{logBean.interfacename,jdbcType=VARCHAR}, #{logBean.accesstype,jdbcType=VARCHAR}," +
            "#{logBean.inputparams,jdbcType=CLOB},#{logBean.outputparams,jdbcType=CLOB},#{logBean.status,jdbcType=VARCHAR})")
    int addLog(@Param("logBean") LogBean logBean); // 添加接口访问日志 // 添加日志

    @Delete("delete from cityreportlog where createtime < sysdate-#{day,jdbcType=INTEGER}")
    void cleanLogByDay(@Param("day")int day);

}
