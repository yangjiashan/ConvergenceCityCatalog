package com.fgi.city.dao;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.bo.OrgBO;
import com.fgi.city.entity.CustomizationInterfaceBean;
import com.fgi.city.entity.CustomizationInterfaceMethodBean;
import com.fgi.city.entity.InterfaceCityBean;
import com.fgi.city.entity.MethodFieldBean;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CityQueryMapper {

    /* start customizationinterface 省平台数据 */
    @Select("select (select orgname from organizationregister where id = orgid) orgname,orgid from customizationinterface ci " +
            "inner join customizationinterface_method cm on ci.id = cm.interfaceid " +
            "where ci.state = '1' and ci.publicity = 'yes' and cm.state = '1' and cm.methodpublicity = 'yes' " +
            "and orgid is not null group by orgid")
    @Results({
            @Result(property = "interfaces", column = "orgid", many = @Many(select = "com.fgi.city.dao.CityQueryMapper.queryInterfaceListByOrgID"))
    })
    List<OrgBO> queryInterfaceList(); // 查询接口列表

    // interfacetype 表示接口类型，设计时候有两种，http、webservice，但是目前我们接口只有webserviec 故先将interfacetype设置为1表示都是webservice接口
    @Select("select *from ( " +
            "select id,id interfaceid, interfacename, interfaceurl interface_url, " +
            "(select 1 from dual) interfacetype, " +
            "(select count(1) from customizationinterface_method cm where cm.interfaceid = c.id and cm.state = '1' and cm.methodpublicity = 'yes') flag " +
            "from customizationinterface c " +
            "where orgid = #{orgid,jdbcType=VARCHAR} and state = '1' and publicity = 'yes' " +
            " ) where flag > 0 ")
    @Results({
            @Result(property = "methods", column = "id", many = @Many(select = "com.fgi.city.dao.CityQueryMapper.queryInterfaceMethodListByInterfaceID"))
    })
    List<CustomizationInterfaceBean> queryInterfaceListByOrgID(@Param("orgid") String orgid); // 根据机构ID查询接口列表

    @Select("select id methodid, methodname from customizationinterface_method where interfaceid = #{interfaceid,jdbcType=VARCHAR} and state = '1' and methodpublicity = 'yes'")
    List<CustomizationInterfaceMethodBean> queryInterfaceMethodListByInterfaceID(@Param("interfaceid") String interfaceid); // 根据接口ID查询方法信息
    /* end customizationinterface 省平台数据 */


    /* start interface_city 地市接口 */
    @Select(" select oc.id orgid, oc.orgname from interface_city ic " +
            " inner join interface_method_city imc on ic.id = imc.interfaceid " +
            " inner join organization_city oc on ic.orgid = oc.id " +
            " where ic.PUBLISHSTATE = '7' and ic.OPENSTATE = '2' and ic.STATE = '1' " +
            " and imc.PUBLISHSTATE = '7' and imc.openstate = '2' and imc.state = '1'" +
            " and oc.id is not null and oc.state = '1' group by oc.id,oc.orgname")
    @Results({
            @Result(property = "interfaces", column = "orgid", many = @Many(select = "com.fgi.city.dao.CityQueryMapper.queryInterfaceCityListByOrgID"))
    })
    List<OrgBO> queryInterfaceCityList(); // 查询接口列表

    // interfacetype 表示接口类型，设计时候有两种，http、webservice
    @Select(" select *from ( " +
            " select ic.id,ic.id interfaceid,ic.interfacename,ic.publishurl interface_url," +
            "  (case when ic.interfacetype = '01' then '1' else '0' end) interfacetype, " +
            " (select count(1) from interface_method_city imc where imc.interfaceid = ic.id  and imc.PUBLISHSTATE = '7' and imc.openstate = '2' and imc.state = '1' ) flag " +
            " from interface_city ic " +
            " where ic.orgid = #{orgid,jdbcType=VARCHAR} and ic.PUBLISHSTATE = '7' and ic.OPENSTATE = '2' and ic.STATE = '1' " +
            ") where flag > 0")
    @Results({
            @Result(property = "methods", column = "id", many = @Many(select = "com.fgi.city.dao.CityQueryMapper.queryInterfaceMethodCityListByInterfaceID"))
    })
    List<CustomizationInterfaceBean> queryInterfaceCityListByOrgID(@Param("orgid") String orgid); // 根据机构ID查询接口列表

    @Select("select imc.id methodid,imc.methodname from interface_method_city imc inner join interface_city ic on imc.interfaceid = ic.id where imc.interfaceid = #{interfaceid,jdbcType=VARCHAR} and ic.INTERFACETYPE = '01' and imc.PUBLISHSTATE = '7' and imc.OPENSTATE = '2' and imc.STATE = '1'")
    List<CustomizationInterfaceMethodBean> queryInterfaceMethodCityListByInterfaceID(@Param("interfaceid") String interfaceid); // 根据接口ID查询方法信息
    /* end interface_city 地市接口 */


    // 省平台
    @Select("select methodname method_ename, methodabbreviation method_cname,methodtype method_type, RESULTEXAMPLES result_example,"
            + " methoddescribe method_desc,RELATIONSHIP relationship, (select type from customizationinterface where id = interfaceid) interfacetype " +
            " from customizationinterface_method where id = #{methodid,jdbcType=VARCHAR} ")
    CustomizationInterfaceMethodBean queryInterfaceMethodByMethodID(@Param("methodid") String methodid); // 根据方法ID查询方法信息

    // 地市
    @Select("select imc.methodname method_ename,imc.methodabbreviation method_cname, imc.methodtype method_type," +
            " imc.resultexample result_example, imc.methoddescribe method_desc, (select 0 from dual) relationship," +
            " (select (case when ic.interfacetype = '01' then '1' else '0' end) from interface_city ic where ic.id = imc.interfaceid) interfacetype " +
            " from interface_method_city imc where imc.id = #{methodid,jdbcType=VARCHAR}")
    CustomizationInterfaceMethodBean queryInterfaceMethodCityByMethodID(@Param("methodid") String methodid); // 根据方法ID查询方法信息


    // 省平台
    @Select(" select b.ename param_ename, b.cname param_cname from customizationinterface_field cf inner join businessindicator b on cf.fieldid = b.id "
            + " where cf.methodid = #{methodFeild.methodid,jdbcType=VARCHAR} and cf.FIELDTYPE = #{methodFeild.fieldtype,jdbcType=VARCHAR}")
    List<MethodFieldBean> queryMethodFieldByMethodIDAuto(@Param("methodFeild") MethodFieldBean methodFeild); // 根据方法ID查询方法参数(配置的方法)

    // 地市
    @Select(" select imfc.paramname param_ename,imfc.describe param_cname from interface_method_field_city imfc where imfc.methodid = #{methodFeild.methodid,jdbcType=VARCHAR} and imfc.fieldtype = #{methodFeild.fieldtype,jdbcType=VARCHAR} and imfc.state = '1' ")
    List<MethodFieldBean> queryMethodFieldCityByMethodIDAuto(@Param("methodFeild") MethodFieldBean methodFeild); // 根据方法ID查询方法参数(配置的方法)

    // 地市
    @Select(" select imfc.paramname param_name,imfc.describe param_desc from interface_method_field_city imfc where imfc.methodid = #{methodFeild.methodid,jdbcType=VARCHAR} and imfc.fieldtype = #{methodFeild.fieldtype,jdbcType=VARCHAR} and imfc.state = '1' ")
    List<MethodFieldBean> queryHttpFieldCityByMethodIDAuto(@Param("methodFeild") MethodFieldBean methodFeild); // 根据方法ID查询方法参数(配置的方法)

    @Select("select param param_ename, paramdescribe param_cname from customizedmethodparam cp where cp.methodid = #{methodid,jdbcType=VARCHAR} ")
    List<MethodFieldBean> queryMethodFieldByMethodID(@Param("methodid") String methodid); // 根据方法ID查询方法参数(人工介入开发的方法)


    // 地市接口(http)
    @Select("select ic.interfacename interface_name,ic.publishurl interface_url,ic.interfacedesc interface_desc, " +
            " (case when ic.interfacetype = '01' then '1' else '0' end) interface_type," +
            " imc.requesttype request_type,imc.paramformat param_format,imc.id methodid " +
            " from interface_city ic inner join interface_method_city imc on ic.id = imc.interfaceid where ic.id = #{interfaceid,jdbcType=VARCHAR} and imc.state = '1' and ic.interfacetype = '02' ")
    List<InterfaceCityBean> queryInterfaceCityInfo(@Param("interfaceid") String interfaceid); // 根据机构ID查询接口详情


    @Select("<script> " +
            "select count(1) from " +
            " (select ic.*,(select oc.areacode from interface_city ifc inner join organization_city oc on ifc.orgid = oc.id where ifc.id = ic.INTERFACEID ) areacode from interfacelog_city ic)  ic " +
            "where ic.interface_name  = #{logBean.interface_name,jdbcType=VARCHAR} and ic.areacode  = #{logBean.areacode,jdbcType=VARCHAR} " +
            "<if test=\"logBean.method_ename != null and logBean.method_ename != ''\"> " +
            " and ic.method_ename = #{logBean.method_ename,jdbcType=VARCHAR} " +
            "</if>" +
            "and to_char(invoke_time,'yyyy/mm/dd') >= to_char(to_date(#{logBean.begin_time, jdbcType=VARCHAR},'yyyy/mm/dd'),'yyyy/mm/dd') " +
            "and to_char(invoke_time,'yyyy/mm/dd') &lt;= to_char(to_date(#{logBean.end_time, jdbcType=VARCHAR},'yyyy/mm/dd'),'yyyy/mm/dd') " +
            "</script> "
    )
    int queryInterfaceLogCount(@Param("logBean") JSONObject logBean); // 查询接口调用日志总数

    @Select("<script> " +
            "select temp1.invoke_organization,temp1.invoke_name,temp1.invoke_account,to_char(temp1.invoke_time,'yyyy/mm/dd hh24:mi:ss') invoke_time,temp1.interface_type,temp1.method_ename,temp1.method_cname from (" +
            "select t.*,rownum rn from (" +
            " select *from (select ic.invoke_org invoke_organization,ic.invoke_name, ic.invoke_account, ic.invoke_time, ic.interface_type, ic.method_ename, ic.method_cname, (select oc.areacode from interface_city ifc inner join organization_city oc on ifc.orgid = oc.id where ifc.id = ic.INTERFACEID ) areacode,ic.interface_name from interfacelog_city ic) ic " +
            "where ic.interface_name  = #{logBean.interface_name,jdbcType=VARCHAR} and ic.areacode  = #{logBean.areacode,jdbcType=VARCHAR} " +
            "<if test=\"logBean.method_ename != null and logBean.method_ename != ''\"> " +
            " and ic.method_ename = #{logBean.method_ename,jdbcType=VARCHAR} " +
            "</if>" +
            "and to_char(ic.invoke_time,'yyyy/mm/dd') >= to_char(to_date(#{logBean.begin_time, jdbcType=VARCHAR},'yyyy/mm/dd'),'yyyy/mm/dd') " +
            "and to_char(ic.invoke_time,'yyyy/mm/dd') &lt;= to_char(to_date(#{logBean.end_time, jdbcType=VARCHAR},'yyyy/mm/dd'),'yyyy/mm/dd') " +
            "order by ic.invoke_time desc)t)temp1 " +
            "where rn between #{logBean.begin, jdbcType=DECIMAL} and #{logBean.end, jdbcType=DECIMAL} "+
            "</script> "
    )
    List<Map<String, String>> queryInterfaceLogPage(@Param("logBean") Map<String, Object> logBean); // 查询接口调用日志（分页）

    @Select("select *from (select temp1.*, rownum rn from (select *from organization_city where singleorgid = #{orgid, jdbcType=VARCHAR} and state = '1' ) temp1) where rn = '1'")
    Map<String, String> queryAreaCodeBySGID(@Param("orgid") String orgid); // 根据机构ID获取地区码， （前提机构已经上报）
}
