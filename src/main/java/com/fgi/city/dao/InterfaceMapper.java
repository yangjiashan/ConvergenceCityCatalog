package com.fgi.city.dao;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.bo.InterfaceBO;
import com.fgi.city.bo.MethodFieldBO;
import com.fgi.city.bo.MethodInfoBO;
import com.fgi.city.entity.AttributeBean;
import com.fgi.city.entity.InterfaceInvokeLogBean;
import com.fgi.city.entity.InterfacePermissionBean;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface InterfaceMapper {

    @Select("select *from interface_method_field_city where methodid = #{methodId,jdbcType=VARCHAR} " +
            " and fieldtype = #{methodType,jdbcType=VARCHAR} and state = '1' "
    )
    List<AttributeBean> queryInterfaceAttribute(@Param("methodId") String methodId, @Param("methodType") String methodType); // 查询接口参数数值

    // 根据接口id获取所有方法以及输入输出参数
    @Select("select *from interface_city where id = #{interfaceId,jdbcType=VARCHAR} ")
    @Results({
            @Result(property = "methodinfos", column = "id", many = @Many(select = "com.fgi.city.dao.InterfaceMapper.queryInterfaceMethods"))
    })
    InterfaceBO queryInterface(@Param("interfaceId") String interfaceId); // 查询接口列表

    @Select("select id, methodname method_ename, methodabbreviation method_cname, methoddescribe method_desc, methodtype method_type, resultexample, createtime, updatetime, " +
            "requesttype, paramformat from interface_method_city where interfaceid = #{interfaceId,jdbcType=VARCHAR} and state = '1' ")
    List<MethodInfoBO> queryInterfaceMethods(@Param("interfaceId") String interfaceId); // 查询接口方法

    @Select("select *from interface_method_field_city where methodid = #{methodId,jdbcType=VARCHAR} and fieldtype = #{paramType,jdbcType=VARCHAR} and state = '1' ")
    List<MethodFieldBO> queryFieldByMethodID(@Param("methodId") String methodId, @Param("paramType") String paramType); // 根据方法ID查询输入参数、输出参数

    @Select("select *from interface_method_field_city where methodid = #{methodId,jdbcType=VARCHAR} and fieldtype = '0' and paramtype = '00' and state = '1' order by sortnum")
    List<MethodFieldBO> queryOhterFieldByMethodId(@Param("methodId") String methodId); // 根据方法ID 查询其他类型的输入参数

    @Select("select *from interface_method_field_city where methodid = #{methodId,jdbcType=VARCHAR} and fieldtype = '0' and paramtype <> '00' and state = '1' order by sortnum")
    List<MethodFieldBO> queryTokenFieldByMethodId(@Param("methodId") String methodId); // 根据方法ID 查询非其他类型的输入参数

    @Select("select imc.methodname,imc.methodabbreviation method_cname,ic.interfaceurl,imc.METHODTYPE,imc.requesttype,imc.paramformat,ic.interfacetype,ic.interfacename,ic.orgid,ic.publishstate interfacepublishstate,imc.publishstate methodpublishstate, ic.publishurl, ic.id interfaceid " +
            "from interface_method_city imc inner join interface_city ic on ic.id = imc.interfaceid where imc.id = #{methodId,jdbcType=VARCHAR}")
    Map<String, String> queryMethodInfoByMethodId(@Param("methodId") String methodId); // 根据方法id查询方法信息

    @Select("select ac.* from accounttable_city ac where areacode = (select (select areacode from organization_city where id = ic.orgid) from interface_method_city imc " +
            "inner join interface_city ic on imc.interfaceid = ic.id where imc.id = #{methodId,jdbcType=VARCHAR})")
    Map<String, String> queryAccountAndPwdByMethodID(@Param("methodId") String methodId); // 根据接口方法ID获取账号密码信息

    @Select("select imc.id,(select count(1) from interface_method_field_city where methodid = imc.id and state = '1') fieldcount from interface_method_city imc where imc.monitorstatus = #{monitorstatus,jdbcType=VARCHAR} and imc.state = '1' ")
    List<Map<String, Object>> queryInterfaceMethodCondition(@Param("monitorstatus") String monitorstatus);

    @Update("update interface_method_city set monitorstatus = #{InterfaceMonitor.code,jdbcType=VARCHAR}, " +
            "monitordesc = #{InterfaceMonitor.message,jdbcType=VARCHAR} where id = #{InterfaceMonitor.id,jdbcType=VARCHAR}")
    int updateInterfaceMonitorStatus(@Param("InterfaceMonitor") JSONObject InterfaceMonitor); // 更新接口探测状态

    @Update("update interface_method_city set monitorstatus = #{InterfaceMonitor.code,jdbcType=VARCHAR}, " +
            "monitordesc = #{InterfaceMonitor.message,jdbcType=VARCHAR} where interfaceid = #{InterfaceMonitor.id,jdbcType=VARCHAR}")
    int updateMonitorStatusByInterface(@Param("InterfaceMonitor") JSONObject InterfaceMonitor); // 根据接口ID, 更新接口探测状态

    @Select("select *from (select a.*, rownum rn from ( " +
            "select *from interface_permission_city " +
            "where methodid = #{permission.methodid,jdbcType=VARCHAR} and account = #{permission.account,jdbcType=VARCHAR} " +
            ") a) where rn = 1")
    InterfacePermissionBean queryInterfacePermissison(@Param("permission") InterfacePermissionBean permission); // 根据方法id、account查询权限

    @Update("update interface_permission_city set visit = #{permission.visit,jdbcType=VARCHAR} where methodid = #{permission.methodid,jdbcType=VARCHAR} and account = #{permission.account,jdbcType=VARCHAR}")
    int updateInterfaceVisit(@Param("permission") InterfacePermissionBean permission); // 更新当日访问次数

    @Insert("insert into interfacelog_city(id, interfaceid, methodid, invoke_org, invoke_account, invoke_name, interface_name, method_cname, invoke_time, method_ename " +
            ", interface_type, platform, platformid) " +
            "values (#{logBean.id,jdbcType=VARCHAR},#{logBean.interfaceid,jdbcType=VARCHAR},#{logBean.methodid,jdbcType=VARCHAR}, " +
            "#{logBean.invoke_org,jdbcType=VARCHAR},#{logBean.invoke_account,jdbcType=VARCHAR},#{logBean.invoke_name,jdbcType=VARCHAR},#{logBean.interface_name,jdbcType=VARCHAR}, " +
            "#{logBean.method_cname,jdbcType=VARCHAR},to_date(#{logBean.invoke_time,jdbcType=VARCHAR},'yyyy/mm/dd hh24:mi:ss'),#{logBean.method_ename,jdbcType=VARCHAR} " +
            ",#{logBean.interface_type,jdbcType=VARCHAR},#{logBean.platform,jdbcType=VARCHAR},#{logBean.platformid,jdbcType=VARCHAR})")
    int addInterfaceInvokeLog(@Param("logBean") InterfaceInvokeLogBean logBean); // 添加接口调用日志

    @Update("update interface_method_city set publishstate = #{interfaceData.publishstate,jdbcType=VARCHAR} where id = #{interfaceData.methodid,jdbcType=VARCHAR}")
    int updateMethodState(@Param("interfaceData") JSONObject interfaceData);// 修改接口发布状态

    @Update("update interface_city set publishstate = #{publishState,jdbcType=VARCHAR} where id = #{interfaceId,jdbcType=VARCHAR}")
    int updateInterfaceState(@Param("interfaceId") String interfaceId, @Param("publishState") String publishState); // 修改接口发布状态

    @Update("update interface_method_city set publishstate = #{publishstate,jdbcType=VARCHAR} where interfaceid = #{interfaceid,jdbcType=VARCHAR} and state = '1' ")
    int updateMethodStateByInterfaceId(@Param("interfaceid") String interfaceid, @Param("publishstate") String publishstate); // 修改接口下所有方法的发布状态

    @Select("select ic.id,ic.interfacename,ic.interfaceurl,ic.interfacetype,ic.openstate interface_openstate, ic.publishstate interface_publishstate, imc.requesttype, imc.paramformat, " +
            " imc.openstate method_openstate, imc.publishstate method_publishstate, imc.id methodid " +
            "from interface_city ic inner join interface_method_city imc on ic.id = imc.interfaceid where ic.id = #{interfaceId,jdbcType=VARCHAR} and imc.state = '1' ")
    Map<String, String> queryMethodByInterfaceId(@Param("interfaceId") String interfaceId); // http根据接口ID查询接口方法信息

    @Select("select *from interface_method_city where id = #{id,jdbcType=VARCHAR}")
    Map<String, String> queryMethodByID(@Param("id") String id); // 根据ID获取接口方法信息

    @Select("select ic.*,(select areacode from organization_city where id = ic.orgid) areacode from interface_city ic where ic.id = (select imc.interfaceid from interface_method_city imc where imc.id = #{methodId,jdbcType=VARCHAR})")
    Map<String, String> queryInterfaceByMethod(@Param("methodId") String methodId); //根据方法ID查询接口信息

    @Select("select count(1) from interface_city where publishurl = #{publishUrl,jdbcType=VARCHAR} and state = '1'")
    int queryPublishUrlExist(@Param("publishUrl") String publishUrl); // 查询发布url是否存在

    @Update("update interface_city set publishurl = #{publishUrl,jdbcType=VARCHAR} where id = #{interfaceId,jdbcType=VARCHAR}")
    int updateInterfacePublishUrl(@Param("interfaceId") String interfaceId, @Param("publishUrl") String publishUrl); // 修改接口发布地址

    @Select("select * from interface_city where id = #{id,jdbcType=VARCHAR}")
    Map<String, String> queryInterfaceById(@Param("id") String id); // 根据接口id查询接口信息

    @Select("select imc.methodname,imc.methodabbreviation method_cname,ic.interfaceurl,imc.METHODTYPE,imc.requesttype,imc.paramformat,ic.interfacetype, " +
            "ic.interfacename,ic.orgid,ic.publishstate interfacepublishstate,imc.publishstate methodpublishstate, ic.publishurl, ic.id interfaceid, imc.id methodid  " +
            "from interface_method_city imc inner join interface_city ic on ic.id = imc.interfaceid where imc.PUBLISHSTATE <> '0' and imc.state = '1' and ic.state = '1' ")
    List<Map<String, String>> queryMethodListByState(); // 发布不是待封装的接口
}
