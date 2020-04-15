package com.fgi.city.dao;

import com.alibaba.fastjson.JSONObject;
import com.fgi.city.entity.DataPreviewInfo;
import com.fgi.city.entity.InterfaceChangeBean;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CityReportMapper {

    @Insert("insert into organization_city(id,orgname,orgshortname,areacode,sortnum,singleorgid) values(#{orgBean.orgId,jdbcType=VARCHAR}, " +
            "#{orgBean.orgname,jdbcType=VARCHAR}, #{orgBean.orgshortname,jdbcType=VARCHAR}, #{orgBean.areacode,jdbcType=VARCHAR}," +
            " #{orgBean.sortnum,jdbcType=VARCHAR}, #{orgBean.singleorgid,jdbcType=VARCHAR})")
    int addOrgCity(@Param("orgBean") JSONObject orgReportBean); // 地市机构目录添加

    @Update("<script> " +
            "update organization_city "
            + "<trim prefix=\"SET\" suffixOverrides=\",\"> "
            + "<if test=\"orgBean.orgname != null and orgBean.orgname != ''\"> "
            + " orgname = #{orgBean.orgname,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"orgBean.orgshortname != null and orgBean.orgshortname != ''\"> "
            + " orgshortname = #{orgBean.orgshortname,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"orgBean.areacode != null and orgBean.areacode != ''\"> "
            + " areacode = #{orgBean.areacode,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"orgBean.sortnum != null and orgBean.sortnum != ''\"> "
            + " sortnum = #{orgBean.sortnum,jdbcType=VARCHAR}, "
            + "</if> "
            + "</trim> "
            + ", updatetime = sysdate "
            + "where orgname = #{orgBean.query_orgname,jdbcType=VARCHAR} and state = '1'"
            + "</script>")
    int updateOrgCity(@Param("orgBean") JSONObject orgReportBean); // 地市机构目录更新

    // 2019/11/5修改为逻辑删除
//    @Delete("delete from organization_city where orgname = #{orgBean.query_orgname}")
    @Update("update organization_city set state = '0' where orgname = #{orgBean.query_orgname} and state = '1'")
    int deleteOrgCity(@Param("orgBean") JSONObject orgReportBean); // 地市机构目录删除

    @Select("select count(1) from organization_city where orgname = #{orgname} and state = '1' ")
    int getCountByOrgName(@Param("orgname") String orgname); // 地市机构目录查询数量

    @Select("select count(1) from organization_city where orgname = #{orgBean.orgname,jdbcType=VARCHAR} and singleorgid = #{orgBean.singleorgid,jdbcType=VARCHAR} and state = '1'")
    int verifyOrgOperator(@Param("orgBean") JSONObject orgReportBean); // 验证机构上报可操作性

    @Select("select areacode from (select t.areacode,rownum rn from (select areacode from organization_city where singleorgid = #{singleId, jdbcType=VARCHAR} and state = '1')t) where rn = 1")
    String getCityCodeBySingleID(@Param("singleId") String singleOrgId);

    @Insert("insert into assetsys_city(id,orgid,sysname,run_network,database_type,sysintroduction,contactdept "
            + ",contactperson,phone,developer_contactdept,developer_contactperson,developer_phone,sortnum) values( "
            + "#{sysBean.sysid,jdbcType=VARCHAR},#{sysBean.orgid,jdbcType=VARCHAR},#{sysBean.sysname,jdbcType=VARCHAR}, "
            + "#{sysBean.run_network,jdbcType=VARCHAR},#{sysBean.databasetype,jdbcType=VARCHAR},#{sysBean.sysintroduction,jdbcType=VARCHAR}, "
            + "#{sysBean.contactdept,jdbcType=VARCHAR},#{sysBean.contactperson,jdbcType=VARCHAR},#{sysBean.phone,jdbcType=VARCHAR}, "
            + "#{sysBean.developer_contactdept,jdbcType=VARCHAR}, #{sysBean.developer_contactperson,jdbcType=VARCHAR}, "
            + "#{sysBean.developer_phone,jdbcType=VARCHAR}, #{sysBean.sortnum,jdbcType=VARCHAR}"
            + ")")
    int addSysCity(@Param("sysBean") JSONObject orgReportBean); // 地市业务系统目录添加

    @Update("<script> " +
            "update assetsys_city "
            + "<trim prefix=\"SET\" suffixOverrides=\",\"> "
            + "<if test=\"sysBean.sysname != null and sysBean.sysname != ''\"> "
            + " sysname = #{sysBean.sysname,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.run_network != null and sysBean.run_network != ''\"> "
            + " run_network = #{sysBean.run_network,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.databasetype != null and sysBean.databasetype != ''\"> "
            + " database_type = #{sysBean.databasetype,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.sysintroduction != null and sysBean.sysintroduction != ''\"> "
            + " sysintroduction = #{sysBean.sysintroduction,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.contactdept != null and sysBean.contactdept != ''\"> "
            + " contactdept = #{sysBean.contactdept,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.contactperson != null and sysBean.contactperson != ''\"> "
            + " contactperson = #{sysBean.contactperson,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.phone != null and sysBean.phone != ''\"> "
            + " phone = #{sysBean.phone,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.developer_contactdept != null and sysBean.developer_contactdept != ''\"> "
            + " developer_contactdept = #{sysBean.developer_contactdept,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.developer_contactperson != null and sysBean.developer_contactperson != ''\"> "
            + " developer_contactperson = #{sysBean.developer_contactperson,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.developer_phone != null and sysBean.developer_phone != ''\"> "
            + " developer_phone = #{sysBean.developer_phone,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"sysBean.sortnum != null and sysBean.sortnum != ''\"> "
            + " sortnum = #{sysBean.sortnum,jdbcType=VARCHAR}, "
            + "</if> "
            + "</trim> "
            + ", updatetime = sysdate "
            + "where orgid = #{sysBean.orgid,jdbcType=VARCHAR} and "
            + "sysname = #{sysBean.query_sysname,jdbcType=VARCHAR} and state = '1' "
            + "</script>")
    int updateSysCity(@Param("sysBean") JSONObject sysReportBean); // 地市业务系统目录更新

    // 2019/11/5 修改为逻辑删除
//    @Delete("delete from assetsys_city where orgid = #{sysBean.orgid} and sysname = #{sysBean.query_sysname}")
    @Update("update assetsys_city set state = '0' where orgid = #{sysBean.orgid} and sysname = #{sysBean.query_sysname} and state = '1'")
    int deleteSysCity(@Param("sysBean") JSONObject sysReportBean); // 地市业务系统目录删除

    @Select("select count(1) from assetsys_city where orgid = #{sysBean.orgid} and sysname = #{sysBean.query_sysname} and state = '1'")
    int countSysCity(@Param("sysBean") JSONObject sysReportBean); //地市业务系统目录数量，判断是否存在

    @Select("select count(1) from assetsys_city where orgid = #{sysBean.orgid} and sysname = #{sysBean.query_sysname} and state = '1'")
    int getCountByQuerySysNameAndOrgId(@Param("sysBean") JSONObject sysReportBean); // 地市业务系统目录查询数量

    @Select("select id from organization_city where orgname = #{orgname} and state = '1'")
    String getOrgIdByOrgName(@Param("orgname") String orgname); // 获取机构名称对应的机构ID

    @Select("select sys.id from organization_city org inner join assetsys_city sys on org.id = sys.orgid where org.orgname = #{catalogBean.orgname} and sys.sysname = #{catalogBean.sysname} and sys.state = '1' and org.state = '1'")
    String verifySysIdExist(@Param("catalogBean") JSONObject catalogBean);// 验证业务系统是否存在，存在则返回业务系统ID，不存在返回空

    @Insert("insert into assetsys_city (id,orgid) values(#{sysBean.sysid,jdbcType=VARCHAR},#{sysBean.orgid,jdbcType=VARCHAR})")
    int addDefaultSysCity(@Param("sysBean") JSONObject orgReportBean); // 创建默认的业务系统

    @Select("select count(1) from catalog_city where sysid = #{catalogBean.sysid,jdbcType=VARCHAR} and catalogname = #{catalogBean.query_catalogname,jdbcType=VARCHAR} and state = '1'")
    int getCatalogCountBySysIDAndCatalogName(@Param("catalogBean") JSONObject catalogBean);

    @Insert("insert into catalog_city(id,sysid,catalogname,sortnum) values(#{catalogBean.catalogid,jdbcType=VARCHAR},#{catalogBean.sysid,jdbcType=VARCHAR}, " +
            "#{catalogBean.catalogname,jdbcType=VARCHAR},#{catalogBean.sortnum,jdbcType=VARCHAR})")
    int addCatalogCity(@Param("catalogBean") JSONObject catalogBean); // 添加业务事项

    @Update("<script> " +
            "update catalog_city " +
            "<trim prefix=\"SET\" suffixOverrides=\",\"> " +
            "<if test=\"catalogBean.catalogname != null and catalogBean.catalogname != ''\"> " +
            "catalogname = #{catalogBean.catalogname,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"catalogBean.sortnum != null and catalogBean.sortnum != ''\"> " +
            "sortnum = #{catalogBean.sortnum,jdbcType=VARCHAR}, " +
            "</if> " +
            "</trim> " +
            ", updatetime = sysdate " +
            "where sysid = #{catalogBean.sysid,jdbcType=VARCHAR} and catalogname = #{catalogBean.query_catalogname,jdbcType=VARCHAR} and state = '1'" +
            "</script>")
    int updateCatalogCity(@Param("catalogBean") JSONObject catalogBean); // 更新业务事项

    // 2019/11/5 修改为逻辑删除
    //@Delete("delete from catalog_city where sysid = #{catalogBean.sysid,jdbcType=VARCHAR} and catalogname = #{catalogBean.query_catalogname,jdbcType=VARCHAR}")
    @Update("update catalog_city set state = '0' where sysid = #{catalogBean.sysid,jdbcType=VARCHAR} and catalogname = #{catalogBean.query_catalogname,jdbcType=VARCHAR} and state = '1'")
    int deleteCatalogCity(@Param("catalogBean") JSONObject catalogBean); // 删除业务事项

    @Select("select count(1) from catalog_city where sysid = #{catalogBean.sysid,jdbcType=VARCHAR} and catalogname = #{catalogBean.query_catalogname,jdbcType=VARCHAR} and state = '1'")
    int countCatalogCity(@Param("catalogBean") JSONObject catalogBean); // 业务事项数量，判断业务事项是否存在

    @Select(" select cata.id from organization_city org inner join assetsys_city asset on org.id = asset.orgid inner join catalog_city cata on asset.id = cata.sysid " +
            " where cata.catalogname = #{MatterBean.catalogname,jdbcType=VARCHAR} and org.orgname = #{MatterBean.orgname,jdbcType=VARCHAR} and asset.id = #{MatterBean.sysid,jdbcType=VARCHAR} and cata.state = '1' and asset.state = '1' and org.state = '1'")
    String verifyCatalogExist(@Param("MatterBean") JSONObject MatterBean);

    @Insert("insert into businessmatter_city(id,catalogid,mattername,tablename,infotype,sortnum) values(#{MatterBean.matterid,jdbcType=VARCHAR},#{MatterBean.catalogid,jdbcType=VARCHAR}," +
            "#{MatterBean.businessmatter_cname,jdbcType=VARCHAR},#{MatterBean.businessmatter_ename,jdbcType=VARCHAR},#{MatterBean.infotype,jdbcType=VARCHAR},#{MatterBean.sortnum,jdbcType=VARCHAR})")
    int addMatterCity(@Param("MatterBean") JSONObject MatterBean);

    @Update("<script> " +
            "update businessmatter_city " +
            "<trim prefix=\"SET\" suffixOverrides=\",\"> " +
            "<if test=\"MatterBean.businessmatter_cname != null and MatterBean.businessmatter_cname != ''\"> " +
            "mattername = #{MatterBean.businessmatter_cname,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"MatterBean.businessmatter_ename != null and MatterBean.businessmatter_ename != ''\"> " +
            "tablename = #{MatterBean.businessmatter_ename,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"MatterBean.infotype != null and MatterBean.infotype != ''\"> " +
            "infotype = #{MatterBean.infotype,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"MatterBean.sortnum != null and MatterBean.sortnum != ''\"> " +
            "sortnum = #{MatterBean.sortnum,jdbcType=VARCHAR}, " +
            "</if> " +
            "</trim> " +
            ", updatetime = sysdate " +
            "where mattername = #{MatterBean.query_businessmatter_cname,jdbcType=VARCHAR} and catalogid = #{MatterBean.catalogid,jdbcType=VARCHAR} and state = '1' " +
            "</script>")
    int updateMatterCity(@Param("MatterBean") JSONObject MatterBean);

//    @Delete("delete from businessmatter_city where mattername = #{MatterBean.query_businessmatter_cname,jdbcType=VARCHAR} and catalogid = #{MatterBean.catalogid,jdbcType=VARCHAR}")
    @Update("update businessmatter_city set state = '0' where mattername = #{MatterBean.query_businessmatter_cname,jdbcType=VARCHAR} and catalogid = #{MatterBean.catalogid,jdbcType=VARCHAR} ")
    int deleteMatterCity(@Param("MatterBean") JSONObject MatterBean);

    @Select("select count(1) from businessmatter_city where mattername = #{MatterBean.query_businessmatter_cname,jdbcType=VARCHAR} and catalogid = #{MatterBean.catalogid,jdbcType=VARCHAR} and state = '1'")
    int countMatterCity(@Param("MatterBean") JSONObject MatterBean); // 判断业务信息是否存在

    @Select("select *from businessmatter_city where mattername = #{MatterBean.query_businessmatter_cname,jdbcType=VARCHAR} and catalogid = #{MatterBean.catalogid,jdbcType=VARCHAR} and state = '1' ")
    Map<String, String> getMatterInfoByMatterNameAndCatalogID(@Param("MatterBean") JSONObject MatterBean); // 根据业务信息名称和业务事项ID获取业务信息

    @Select("select count(1) from businessmatter_city where mattername = #{MatterBean.query_businessmatter_cname,jdbcType=VARCHAR} and catalogid = #{MatterBean.catalogid,jdbcType=VARCHAR} and state = '1' ")
    int getMatterCountByMatternameAndCatalogId(@Param("MatterBean") JSONObject MatterBean);

    @Select(" select bc.id from organization_city org inner join assetsys_city asset on org.id = asset.orgid inner join catalog_city cata on asset.id = cata.sysid " +
            "inner join businessmatter_city bc on bc.catalogid = cata.id where cata.catalogname = #{IndicatorBean.catalogname,jdbcType=VARCHAR} and cata.state = '1' " +
            "and org.orgname = #{IndicatorBean.orgname,jdbcType=VARCHAR} and org.state = '1' and bc.mattername = #{IndicatorBean.businessmatter_cname,jdbcType=VARCHAR} and bc.state = '1' and asset.id = #{IndicatorBean.sysid,jdbcType=VARCHAR} and asset.state = '1' ")
    String verifyMatterExist(@Param("IndicatorBean") JSONObject IndicatorBean);

    @Insert("insert into businessindicator_city (id,matterid, ename, cname, fieldtype, fieldlength, ispk, sortnum, isnull) values(" +
            "#{IndicatorBean.indicatorid,jdbcType=VARCHAR}, #{IndicatorBean.matterid,jdbcType=VARCHAR}, #{IndicatorBean.indicator_ename,jdbcType=VARCHAR}, #{IndicatorBean.indicator_cname,jdbcType=VARCHAR}" +
            ",#{IndicatorBean.indicatortype,jdbcType=VARCHAR}, #{IndicatorBean.indicatorlength,jdbcType=VARCHAR}, #{IndicatorBean.ispk,jdbcType=VARCHAR}," +
            "#{IndicatorBean.sortnum,jdbcType=VARCHAR},#{IndicatorBean.isnull,jdbcType=VARCHAR})")
    int addIndicatorCity(@Param("IndicatorBean") JSONObject IndicatorBean); // 添加指标项

    @Update("<script> " +
            "update businessindicator_city " +
            "<trim prefix=\"SET\" suffixOverrides=\",\"> " +
            "<if test=\"IndicatorBean.indicator_ename != null and IndicatorBean.indicator_ename != ''\"> " +
            "ename = #{IndicatorBean.indicator_ename,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"IndicatorBean.indicator_cname != null and IndicatorBean.indicator_cname != ''\"> " +
            "cname = #{IndicatorBean.indicator_cname,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"IndicatorBean.indicatortype != null and IndicatorBean.indicatortype != ''\"> " +
            "fieldtype = #{IndicatorBean.indicatortype,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"IndicatorBean.indicatorlength != null and IndicatorBean.indicatorlength != ''\"> " +
            "fieldlength = #{IndicatorBean.indicatorlength,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"IndicatorBean.ispk != null and IndicatorBean.ispk != ''\"> " +
            "ispk = #{IndicatorBean.ispk,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"IndicatorBean.sortnum != null and IndicatorBean.sortnum != ''\"> " +
            "sortnum = #{IndicatorBean.sortnum,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"IndicatorBean.isnull != null and IndicatorBean.isnull != ''\"> " +
            "isnull = #{IndicatorBean.isnull,jdbcType=VARCHAR}, " +
            "</if> " +
            "</trim> " +
            ", updatetime = sysdate " +
            "where ename = #{IndicatorBean.query_indicator_ename,jdbcType=VARCHAR} and matterid = #{IndicatorBean.matterid,jdbcType=VARCHAR} and state = '1' " +
            "</script>")
    int updateIndicatorCity(@Param("IndicatorBean") JSONObject IndicatorBean); // 更新指标项

//    @Delete("delete from businessindicator_city where ename = #{IndicatorBean.query_indicator_ename,jdbcType=VARCHAR} and matterid = #{IndicatorBean.matterid,jdbcType=VARCHAR} ")
    @Update("update businessindicator_city set state = '0' where ename = #{IndicatorBean.query_indicator_ename,jdbcType=VARCHAR} and matterid = #{IndicatorBean.matterid,jdbcType=VARCHAR} and state = '1' ")
    int deleteIndicatorCity(@Param("IndicatorBean") JSONObject IndicatorBean); // 删除指标项

    @Select("select count(1) from businessindicator_city where ename = #{IndicatorBean.query_indicator_ename,jdbcType=VARCHAR} and matterid = #{IndicatorBean.matterid,jdbcType=VARCHAR} and state = '1'")
    int getIndicatorCountByEnameAndMatterId(@Param("IndicatorBean") JSONObject IndicatorBean); // 根据ename、matterid查询指标项数量

    @Select("select * from businessmatter_city where id = #{matterid,jdbcType=VARCHAR}")
    Map getBusinessMatterByID(@Param("matterid") String matterid); // 根据业务信息ID获取业务信息

    @Select("select bic.* from businessmatter_city bc inner join businessindicator_city bic on bc.id = bic.matterid where bc.id = #{matterid,jdbcType=VARCHAR} and bc.state = '1' and bic.state = '1' ")
    List<Map<String, String>> getBusinessIndicatorByMatterID(@Param("matterid") String matterid); // 根据业务信息ID获取指标项

    @Update("update businessmatter_city set datapreview = '' where id = #{matterid,jdbcType=VARCHAR}")
    int deleteDataPreviewByMatterID(@Param("matterid") String matterid); // 删除业务信息预览数据

    @Update("update businessmatter_city set datapreview = #{matterData.datapreview, jdbcType=CLOB} where id = #{matterData.matterid,jdbcType=VARCHAR}")
    int addDataPreviewByMatterID(@Param("matterData") JSONObject matterData); // 添加业务信息预览数据

    @Select("select count(1) from interface_city where orgid = #{interfaceData.orgid,jdbcType=VARCHAR} and interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and state = '1'")
    int getInterfaceCountByOrgIDAndName(@Param("interfaceData") JSONObject interfaceData); // 根据机构ID和接口名称查询接口数量

    @Insert("insert into interface_city(id,orgid,interfacename,interfaceshortname,interfacetype,sortnum,interfaceurl,interfacedesc,sender_id,authorized_id,service_id) values(#{interfaceData.interfaceid,jdbcType=VARCHAR}," +
            "#{interfaceData.orgid,jdbcType=VARCHAR},#{interfaceData.interface_name,jdbcType=VARCHAR},#{interfaceData.interface_sname,jdbcType=VARCHAR},#{interfaceData.interface_type,jdbcType=VARCHAR}," +
            "#{interfaceData.sortnum,jdbcType=VARCHAR},#{interfaceData.interface_url,jdbcType=VARCHAR},#{interfaceData.interface_desc,jdbcType=VARCHAR},#{interfaceData.sender_id,jdbcType=VARCHAR} " +
            ",#{interfaceData.authorized_id,jdbcType=VARCHAR},#{interfaceData.service_id,jdbcType=VARCHAR})")
    int addInterfaceCity(@Param("interfaceData") JSONObject interfaceData); // 添加地市接口(http)

    @Update("<script> " +
            "update interface_city " +
            "<trim prefix=\"SET\" suffixOverrides=\",\"> " +
            "<if test=\"interfaceData.interface_name != null and interfaceData.interface_name != ''\"> " +
            "interfacename = #{interfaceData.interface_name,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.interface_type != null and interfaceData.interface_type != ''\"> " +
            "interfacetype = #{interfaceData.interface_type,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.sortnum != null and interfaceData.sortnum != ''\"> " +
            "sortnum = #{interfaceData.sortnum,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.interface_url != null and interfaceData.interface_url != ''\"> " +
            "interfaceurl = #{interfaceData.interface_url,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.interface_desc != null and interfaceData.interface_desc != ''\"> " +
            "interfacedesc = #{interfaceData.interface_desc,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.interface_sname != null and interfaceData.interface_sname != ''\"> " +
            "interfaceshortname = #{interfaceData.interface_sname,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.sender_id != null \"> " +
            "sender_id = #{interfaceData.sender_id,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.authorized_id != null \"> " +
            "authorized_id = #{interfaceData.authorized_id,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceData.service_id != null \"> " +
            "service_id = #{interfaceData.service_id,jdbcType=VARCHAR}, " +
            "</if> " +
            "</trim> " +
            ", updatetime = sysdate " +
            "where interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and orgid = #{interfaceData.orgid,jdbcType=VARCHAR} and state = '1'" +
            "</script>")
    int updateInterfaceCity(@Param("interfaceData") JSONObject interfaceData); // 更新地市接口

    @Select("select id from interface_city where interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and orgid = #{interfaceData.orgid,jdbcType=VARCHAR} and state = '1'")
    String getInterfaceIdByOrgIDAndInterfaceName(@Param("interfaceData") JSONObject interfaceData);// 查询接口ID

    @Select("select *from interface_city where interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and orgid = #{interfaceData.orgid,jdbcType=VARCHAR} and state = '1'")
    Map<String, String> queryInterfaceInfoByName(@Param("interfaceData") JSONObject interfaceData);// 根据接口名称查询接口信息

    // 修改为逻辑删除
//    @Delete("delete from interface_city where interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and orgid = #{interfaceData.orgid,jdbcType=VARCHAR}")
    @Update("update interface_city set state = '0' where interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and orgid = #{interfaceData.orgid,jdbcType=VARCHAR} and state = '1'")
    int deleteInterfaceCity(@Param("interfaceData") JSONObject interfaceData); // 删除地市接口


    @Select("select ic.id from interface_city ic inner join organization_city oc on ic.orgid = oc.id where ic.interfacename = #{interfaceData.interface_name,jdbcType=VARCHAR} and ic.state = '1' " +
            "and oc.orgname = #{interfaceData.orgname,jdbcType=VARCHAR}")
    String verifyInterfaceIDExist(@Param("interfaceData") JSONObject interfaceData); // 验证接口名称是否存在

    @Insert("insert into interface_method_city(id,interfaceid,methodname,methodabbreviation,methoddescribe,querytable,resultexample,methodtype,sortnum) values(" +
            "#{interfaceMethodData.methodid,jdbcType=VARCHAR},#{interfaceMethodData.interfaceid,jdbcType=VARCHAR}," +
            "#{interfaceMethodData.method,jdbcType=VARCHAR},#{interfaceMethodData.method_cname,jdbcType=VARCHAR}," +
            "#{interfaceMethodData.method_describe,jdbcType=VARCHAR},#{interfaceMethodData.querytable,jdbcType=VARCHAR}," +
            "#{interfaceMethodData.result_example,jdbcType=VARCHAR},#{interfaceMethodData.method_type,jdbcType=VARCHAR},#{interfaceMethodData.sortnum,jdbcType=VARCHAR})")
    int addInterfaceMethodCity(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 添加接口方法


    @Insert("insert into interface_method_city(id,interfaceid,querytable,resultexample,methodtype,REQUESTTYPE,PARAMFORMAT) values(" +
            "#{interfaceMethodData.methodid,jdbcType=VARCHAR},#{interfaceMethodData.interfaceid,jdbcType=VARCHAR}," +
            "#{interfaceMethodData.querytable,jdbcType=VARCHAR}," +
            "#{interfaceMethodData.result_example,jdbcType=VARCHAR},#{interfaceMethodData.operation_type,jdbcType=VARCHAR}," +
            "#{interfaceMethodData.request_type,jdbcType=VARCHAR},#{interfaceMethodData.param_format,jdbcType=VARCHAR})")
    int addHttpInterfaceMethodCity(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 添加http接口具体信息

    @Update("<script> " +
            "update interface_method_city " +
            "<trim prefix=\"SET\" suffixOverrides=\",\"> " +
            "<if test=\"interfaceMethodData.method != null and interfaceMethodData.method != ''\"> " +
            "methodname = #{interfaceMethodData.method,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.method_cname != null and interfaceMethodData.method_cname != ''\"> " +
            "methodabbreviation = #{interfaceMethodData.method_cname,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.method_describe != null and interfaceMethodData.method_describe != ''\"> " +
            "methoddescribe = #{interfaceMethodData.method_describe,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.method_type != null and interfaceMethodData.method_type != ''\"> " +
            "methodtype = #{interfaceMethodData.method_type,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.querytable != null and interfaceMethodData.querytable != ''\"> " +
            "querytable = #{interfaceMethodData.querytable,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.sortnum != null and interfaceMethodData.sortnum != ''\"> " +
            "sortnum = #{interfaceMethodData.sortnum,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.result_example != null and interfaceMethodData.result_example != ''\"> " +
            "resultexample = #{interfaceMethodData.result_example,jdbcType=VARCHAR}, " +
            "</if> " +
            "</trim> " +
            ", updatetime = sysdate " +
            "where methodname = #{interfaceMethodData.query_method,jdbcType=VARCHAR} and interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR} and state = '1'" +
            "</script>")
    int updateInterfaceMethodCity(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 更新接口方法

    @Select("select *from interface_method_city where methodname = #{interfaceMethodData.query_method,jdbcType=VARCHAR} and interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR} and state = '1'")
    Map<String, String> queryMethodInfoByName(@Param("interfaceMethodData") JSONObject interfaceMethodData);

    @Update("<script> " +
            "update interface_method_city " +
            "<trim prefix=\"SET\" suffixOverrides=\",\"> " +
            "<if test=\"interfaceMethodData.querytable != null and interfaceMethodData.querytable != ''\"> " +
            "querytable = #{interfaceMethodData.querytable,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.result_example != null and interfaceMethodData.result_example != ''\"> " +
            "resultexample = #{interfaceMethodData.result_example,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.operation_type != null and interfaceMethodData.operation_type != ''\"> " +
            "methodtype = #{interfaceMethodData.operation_type,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.request_type != null and interfaceMethodData.request_type != ''\"> " +
            "requesttype = #{interfaceMethodData.request_type,jdbcType=VARCHAR}, " +
            "</if> " +
            "<if test=\"interfaceMethodData.param_format != null and interfaceMethodData.param_format != ''\"> " +
            "paramformat = #{interfaceMethodData.param_format,jdbcType=VARCHAR}, " +
            "</if> " +
            "</trim> " +
            ", updatetime = sysdate " +
            "where interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR} and state = '1'" +
            "</script>")
    int updateHttpInterfaceMethodCity(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 更新http接口具体信息

    // 修改为逻辑删除
    // @Delete("delete from interface_method_city where methodname = #{interfaceMethodData.query_method,jdbcType=VARCHAR} and interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR}")
    @Update("update interface_method_city set state = '0' where methodname = #{interfaceMethodData.query_method,jdbcType=VARCHAR} and interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR} and state = '1'")
    int deleteInterfaceMethodCity(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 删除接口方法

    @Select("select count(1) from interface_method_city where methodname = #{interfaceMethodData.query_method,jdbcType=VARCHAR} and interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR} and state = '1'")
    int getInterfaceMethodCountByInterfaceidAndMethodName(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 根据接口方法名称和接口ID查询数量

    @Select("select id from interface_method_city where methodname = #{interfaceMethodData.query_method,jdbcType=VARCHAR} and interfaceid = #{interfaceMethodData.interfaceid,jdbcType=VARCHAR} and state = '1' ")
    String getInterfaceMethodIDByInterfaceIDAndMethodName(@Param("interfaceMethodData") JSONObject interfaceMethodData); // 根据接口方法名称和接口ID查询接口方法ID

    @Select("select id from interface_method_city where interfaceid = #{interfaceData.interfaceid,jdbcType=VARCHAR}")
    String getHttpIDByInterfaceID(@Param("interfaceData") JSONObject interfaceData); // 根据接口方法名称和接口ID查询接口方法ID


    @Insert("insert into interface_method_field_city(id,methodid,fieldtype,sortnum,paramname,paramtype,describe,paramvalue) values(" +
            "#{MethodFieldData.fieldid,jdbcType=VARCHAR},#{MethodFieldData.methodid,jdbcType=VARCHAR}," +
            "#{MethodFieldData.fieldtype,jdbcType=VARCHAR},#{MethodFieldData.sortnum,jdbcType=VARCHAR}," +
            "#{MethodFieldData.param_name,jdbcType=VARCHAR},#{MethodFieldData.param_type,jdbcType=VARCHAR}," +
            "#{MethodFieldData.param_describe,jdbcType=VARCHAR},#{MethodFieldData.param_value,jdbcType=VARCHAR})")
    int addInterfaceMethodFieldCity(@Param("MethodFieldData") JSONObject MethodFieldData); // 添加接口方法参数

    @Select("select *from interface_method_field_city where id = #{fieldId,jdbcType=VARCHAR} ")
    Map<String, String> queryFieldInfoById(@Param("fieldId") String fieldId); // 根据ID查询方法参数信息

    @Update("<script> " +
            "update interface_method_field_city "
            + "<trim prefix=\"SET\" suffixOverrides=\",\"> "
            + "<if test=\"FieldData.sortnum != null and FieldData.sortnum != ''\"> "
            + " sortnum = #{FieldData.sortnum,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"FieldData.param_type != null and FieldData.param_type != ''\"> "
            + " paramtype = #{FieldData.param_type,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"FieldData.param_describe != null and FieldData.param_describe != ''\"> "
            + " describe = #{FieldData.param_describe,jdbcType=VARCHAR}, "
            + "</if> "
            + "<if test=\"FieldData.param_value != null and FieldData.param_value != ''\"> "
            + " paramvalue = #{FieldData.param_value,jdbcType=VARCHAR}, "
            + "</if> "
            + "</trim> "
            + ", updatetime = sysdate "
            + "where methodid = #{FieldData.methodid,jdbcType=VARCHAR} and fieldtype = #{FieldData.fieldtype,jdbcType=VARCHAR} " +
            "  and paramname = #{FieldData.param_name,jdbcType=VARCHAR} and state = '1'"
            + "</script>")
    int updateInterfaceMethodFieldCity(@Param("FieldData") JSONObject MethodFieldData); // 根据参数名称修改接口方法参数

    @Select("select *from interface_method_field_city where methodid = #{FieldData.methodid,jdbcType=VARCHAR} and fieldtype = #{FieldData.fieldtype,jdbcType=VARCHAR} " +
            " and paramname = #{FieldData.param_name,jdbcType=VARCHAR} and state = '1'")
    Map<String, String> queryMethodFieldByName(@Param("FieldData") JSONObject MethodFieldData); // 根据名称查询参数信息

    @Select("select *from interface_method_field_city where methodid = #{MethodFieldData.methodid,jdbcType=VARCHAR} and state = '1'")
    List<Map<String, String>> queryMethodFieldByMethodId(@Param("MethodFieldData") JSONObject MethodFieldData);

    @Select("select *from interface_method_field_city where methodid = #{methodId,jdbcType=VARCHAR} and fieldtype = #{fieldtype,jdbcType=VARCHAR} and state = '1'")
    List<Map<String, String>> queryIOFieldByMethodId(@Param("methodId") String methodId, @Param("fieldtype") String fieldtype);

    // 修改为逻辑删除
//    @Delete("delete from interface_method_field_city where methodid = #{MethodFieldData.methodid,jdbcType=VARCHAR}")
    @Update("update interface_method_field_city set state = '0' where methodid = #{MethodFieldData.methodid,jdbcType=VARCHAR} and state = '1' ")
    int deleteInterfaceMethodFieldCity(@Param("MethodFieldData") JSONObject MethodFieldData); // 删除接口方法参数

    // 逻辑删除
    @Update("update interface_method_field_city set state = '0' where id = #{fieldId,jdbcType=VARCHAR}")
    int deleteMethodFieldById(@Param("fieldId") String fieldId); // 删除接口方法参数


//    @Delete("delete from interface_method_field_city imfc where imfc.methodid in " +
//            "(select id from interface_method_city imc where imc.interfaceid = #{MethodData.interfaceid,jdbcType=VARCHAR} " +
//            " and imc.methodname = #{MethodData.methodname,jdbcType=VARCHAR})")
//    int deleteInterfaceMethodFieldByInterface(@Param("MethodData") JSONObject MethodData); // 根据methodname、interfaceid删除方法参数

    @Select("select imc.* from interface_method_city imc inner join interface_city ic on imc.interfaceid = ic.id where " +
            "ic.interfacename = #{interfaceData.query_interface_name,jdbcType=VARCHAR} and ic.orgid = #{interfaceData.orgid,jdbcType=VARCHAR} and ic.state = '1' and imc.state = '1'")
    List<Map<String, String>> getInterfaceMethodByInterface(@Param("interfaceData") JSONObject interfaceData); // 根据接口名称和结构ID获取接口下所有的方法


    @Select("select *from interface_method_city where id = #{interfaceData.methodid,jdbcType=VARCHAR} ")
    Map<String, String> queryInterfaceMethodByID(@Param("interfaceData") JSONObject interfaceData);

    // 修改为逻辑删除
//    @Delete("delete from interface_method_city where id = #{interfaceData.methodid,jdbcType=VARCHAR}")
    @Update("update interface_method_city set state = '0' where id = #{interfaceData.methodid,jdbcType=VARCHAR}")
    int deleteInterfaceMethodByMethodID(@Param("interfaceData") JSONObject interfaceData); // 删除接口方法

    @Update("update interface_method_city set state = '0' where interfaceid = #{interfaceId,jdbcType=VARCHAR}")
    int deleteMethodByInterfaceID(@Param("interfaceId") String interfaceid); // 删除接口下所有方法

    @Select("select ic.* from interface_city ic inner join organization_city oc on ic.orgid = oc.id where oc.orgname  = #{orgData.orgname,jdbcType=VARCHAR} and ic.state = '1' ")
    List<Map<String, String>> getInterfaceByOrgID(@Param("orgData") JSONObject orgData);

    @Select("select *from interface_method_city where interfaceid = #{interfaceData.interfaceid,jdbcType=VARCHAR} and state = '1' ")
    List<Map<String, String>> getInterfaceMethodByInterfaceID(@Param("interfaceData") JSONObject interfaceData);

    // 修改为逻辑删除
    // @Delete("delete from interface_city where id = #{interfaceData.interfaceid,jdbcType=VARCHAR}")
    @Update("update interface_city set state = '0' where id = #{interfaceData.interfaceid,jdbcType=VARCHAR}")
    int deleteInterfaceByInterfaceID(@Param("interfaceData") JSONObject interfaceData);

    // 2019/11/5 修改为逻辑删除
//    @Delete("delete from businessindicator_city where matterid = #{indicatorData.matterid,jdbcType=VARCHAR}")
    @Update("update businessindicator_city set state = '0' where matterid = #{indicatorData.matterid,jdbcType=VARCHAR} and state = '1' ")
    int deleteIndicatorByMatterID(@Param("indicatorData") JSONObject interfaceData); // 根据业务信息ID删除指标项

    @Select("select bc.* from businessmatter_city bc inner join catalog_city cc on bc.catalogid = cc.id where " +
            "cc.sysid = #{catalogBean.sysid,jdbcType=VARCHAR} and cc.catalogname = #{catalogBean.query_catalogname,jdbcType=VARCHAR} and bc.state = '1' ")
    List<Map<String, String>> getMattersByCatalogName(@Param("catalogBean") JSONObject catalogBean); // 根据业务事项名称获取业务信息

    // 2019/11/5 修改为逻辑删除
//    @Delete("delete from businessmatter_city where id = #{catalogBean.matterid,jdbcType=VARCHAR}")
    @Update("update businessmatter_city set state = '0' where id = #{catalogBean.matterid,jdbcType=VARCHAR} and state = '1' ")
    int deleteMatterByMatterID(@Param("catalogBean") JSONObject catalogBean); // 根据业务信息ID删除业务信息

    @Select("select cc.* from catalog_city cc inner join assetsys_city ac on cc.sysid = ac.id where " +
            "ac.sysname = #{sysBean.query_sysname,jdbcType=VARCHAR} and ac.orgid = #{sysBean.orgid,jdbcType=VARCHAR} and cc.state = '1' ")
    List<Map<String, String>> getCatalogBySysName(@Param("sysBean") JSONObject sysBean); // 根据系统名称获取业务事项

    @Select("select *from businessmatter_city where catalogid = #{sysBean.catalogid,jdbcType=VARCHAR} and state = '1' ")
    List<Map<String, String>> getMattersByCatalogID(@Param("sysBean") JSONObject sysBean); // 根据业务事项ID获取业务信息

    // 2019/11/5 修改为逻辑删除
//    @Delete("delete from catalog_city where id = #{sysBean.catalogid,jdbcType=VARCHAR}")
    @Update("update catalog_city set state = '0' where id = #{sysBean.catalogid,jdbcType=VARCHAR} ")
    int deleteCatalogByCatalogID(@Param("sysBean") JSONObject sysBean); // 根据业务事项ID删除业务事项

    @Select("select ac.* from assetsys_city ac inner join organization_city oc on ac.orgid = oc.id where " +
            "oc.orgname = #{orgBean.query_orgname,jdbcType=VARCHAR} and ac.state = '1' ")
    List<Map<String, String>> getSysByOrgname(@Param("orgBean") JSONObject orgBean); // 根据机构获取业务系统

    @Select("select *from catalog_city where sysid = #{orgBean.sysid,jdbcType=VARCHAR} and state = '1' ")
    List<Map<String, String>> getCatalogsBySysID(@Param("orgBean") JSONObject orgBean); // 根据业务系统ID获取业务事项

    // 2019/11/5 修改为逻辑删除
//    @Delete("delete from assetsys_city where id = #{orgBean.sysid,jdbcType=VARCHAR}")
    @Update("update assetsys_city set state = '0' where id = #{orgBean.sysid,jdbcType=VARCHAR} ")
    int deleteSysBySysID(@Param("orgBean") JSONObject orgBean); // 根据业务系统ID删除业务系统

    @Insert("insert into accounttable_city(id,orgid,username,password,methodid,areacode) values(" +
            "#{accountBean.id,jdbcType=VARCHAR},#{accountBean.orgid,jdbcType=VARCHAR},#{accountBean.username,jdbcType=VARCHAR}" +
            ",#{accountBean.password,jdbcType=VARCHAR},#{accountBean.methodid,jdbcType=VARCHAR},#{accountBean.areacode,jdbcType=VARCHAR})")
    int addAccountAndPwdCity(@Param("accountBean") JSONObject accountBean); //添加账号密码

    @Update("<script> " +
            "update accounttable_city set username = #{accountBean.username,jdbcType=VARCHAR}, " +
            "password = #{accountBean.password,jdbcType=VARCHAR} " +
            "<if test=\"accountBean.methodid != null and accountBean.methodid != ''\"> " +
            ", methodid = #{accountBean.methodid,jdbcType=VARCHAR} " +
            "</if> " +
            ", updatetime = sysdate " +
            "where orgid = #{accountBean.orgid,jdbcType=VARCHAR}" +
            "</script> "
    )
    int updateAccountAndPwdCity(@Param("accountBean") JSONObject accountBean); //更新账号密码

    @Delete("delete from accounttable_city where orgid = #{accountBean.orgid,jdbcType=VARCHAR}")
    int deleteAccountAndPwdCity(@Param("accountBean") JSONObject accountBean); // 删除该机构上报的账号密码

    @Select("<script> " +
            "select ic.id interfaceid,ic.interfacename,imc.id methodid,imc.methodname from interface_city ic inner join interface_method_city imc on ic.id = imc.interfaceid " +
            "where ic.orgid = #{methodBean.orgid,jdbcType=VARCHAR} " +
            " and ic.interfacename = #{methodBean.token_interface_name,jdbcType=VARCHAR} " +
            " and ic.interfacetype = #{methodBean.interfacetype,jdbcType=VARCHAR} and ic.state = '1' and imc.state = '1' " +
            "<if test=\"methodBean.token_method_name != null and methodBean.token_method_name != ''\"> " +
            " and methodname = #{methodBean.token_method_name,jdbcType=VARCHAR} " +
            "</if> " +
            "</script> "
    )
    List<Map<String, String>> getMethodIdByMethodName(@Param("methodBean") JSONObject methodBean); // 根据方法名称获取方法ID

    @Insert("insert into interfacelog_city (id,interfaceid,methodid,invoke_org,invoke_account,invoke_name,interface_name,method_cname,invoke_time,method_ename,interface_type,report_org,report_account,report_orgid) values( " +
            "#{logBean.id,jdbcType=VARCHAR},#{logBean.interfaceid,jdbcType=VARCHAR},#{logBean.methodid,jdbcType=VARCHAR}," +
            "#{logBean.invoke_organization,jdbcType=VARCHAR},#{logBean.invoke_account,jdbcType=VARCHAR},#{logBean.invoke_name,jdbcType=VARCHAR}," +
            "#{logBean.interfacename,jdbcType=VARCHAR},#{logBean.methodname,jdbcType=VARCHAR},to_date(#{logBean.invoke_time,jdbcType=VARCHAR},'yyyy/mm/dd hh24:mi:ss'), " +
            "#{logBean.methodename,jdbcType=VARCHAR},#{logBean.interfacetype,jdbcType=VARCHAR},#{logBean.report_org,jdbcType=VARCHAR},#{logBean.report_account,jdbcType=VARCHAR},#{logBean.report_orgid,jdbcType=VARCHAR} " +
            ")")
    int addInterfaceLogCity(@Param("logBean") JSONObject logBean); // 添加接口调用日志

    @Select("select i.interfaceid,i.id methodid,i.methodabbreviation methodname,i.methodname methodename,ic.interfacename,ic.interfacetype from interface_method_city i inner join interface_city ic on i.interfaceid = ic.id where i.id = #{methodId,jdbcType=VARCHAR} and i.state = '1' ")
    Map<String, String> getInterfaceMethodById(@Param("methodId") String methodId); // 根据接口方法ID查询接口方法

    @Select("select imc.interfaceid,imc.id methodid,imc.methodabbreviation methodname,imc.methodname methodename,ic.interfacename,ic.interfacetype from interface_city ic inner join interface_method_city imc on ic.id = imc.interfaceid where ic.id = #{interfaceId,jdbcType=VARCHAR} and imc.state = '1' ")
    List<Map<String, String>> getInterfaceById(@Param("interfaceId") String interfaceId); // 根据接口ID查询接口信息

    @Select("select *from interface_city where id = #{interfaceId,jdbcType=VARCHAR}")
    Map<String, String> getInterfaceInfoById(@Param("interfaceId") String interfaceId); // 根据接口ID查询接口信息

    @Insert("insert into interface_change_city (id,changetableid,changetable,changecolumn,oldvalue,newvalue,type,creatorid,flag,batch) values ( " +
            "#{changeBean.id,jdbcType=VARCHAR},#{changeBean.changetableid,jdbcType=VARCHAR},#{changeBean.changetable,jdbcType=VARCHAR}," +
            "#{changeBean.changecolumn,jdbcType=VARCHAR},#{changeBean.oldvalue,jdbcType=VARCHAR},#{changeBean.newvalue,jdbcType=VARCHAR}," +
            "#{changeBean.type,jdbcType=VARCHAR},#{changeBean.creatorid,jdbcType=VARCHAR},#{changeBean.flag,jdbcType=VARCHAR},#{changeBean.batch,jdbcType=VARCHAR} " +
            ")")
    int addInterfaceChangeCity(@Param("changeBean") InterfaceChangeBean changeBean); // 添加接口变更记录

    @Update("update organization_city set icon = #{orgBean.icon,jdbcType=BLOB} where orgname = #{orgBean.orgname,jdbcType=VARCHAR} and state = '1' ")
    int updateOrgIcon(@Param("orgBean") JSONObject orgBean); // 更新机构图标

    @Select("select *from organization_city where singleorgid = #{sgid,jdbcType=VARCHAR} and state = '1' ")
    List<Map<String, String>> queryOrganizationBySgId(@Param("sgid") String sgid); // 查询机构信息根据单点机构id

    @Select("select *from datapreview_city where areacode = #{areacode,jdbcType=VARCHAR}")
    DataPreviewInfo queryDataPreviewByAreaCode(@Param("areacode") String areacode) ; //查询接口数据预览接口信息

    @Insert("insert into datapreview_city(id, areacode, dataurl, reportorg, reportaccount) values(" +
            "#{data.id,jdbcType=VARCHAR},#{data.areacode,jdbcType=VARCHAR},#{data.dataurl,jdbcType=VARCHAR}," +
            "#{data.reportorg,jdbcType=VARCHAR},#{data.reportaccount,jdbcType=VARCHAR}" +
            ")")
    int insertDataPreviewUrl(@Param("data") DataPreviewInfo dataPreviewInfo); // 新增数据预览接口地址

    @Update("update datapreview_city set dataurl = #{data.dataurl,jdbcType=VARCHAR}, reportorg = #{data.reportorg,jdbcType=VARCHAR}, " +
            "reportaccount = #{data.reportaccount,jdbcType=VARCHAR}, updatetime = sysdate where areacode = #{data.areacode,jdbcType=VARCHAR}")
    int updateDataPreviewUrl(@Param("data") DataPreviewInfo dataPreviewInfo); // 更新数据预览接口地址

    @Select("select *from assetsys_city where sysname is null and orgid = #{orgid,jdbcType=VARCHAR} and state = '1' ")
    Map<String, String> querySysDefaultExist(@Param("orgid")  String orgid);

    @Select("select o1.id id1,o2.id id2,o3.id id3,o4.id id4,o5.id id5 from organizationregister o1 " +
            " left join organizationregister o2 on o1.parentid = o2.id " +
            " left join organizationregister o3 on o2.parentid = o3.id " +
            " left join organizationregister o4 on o3.parentid = o4.id " +
            " left join organizationregister o5 on o4.parentid = o5.id where o1.id = #{orgid,jdbcType=VARCHAR}")
    Map<String, String> querySgOrgId(@Param("orgid") String orgid); // 查询机构的父级机构（暂时用5级）

    @Select(" select cm.interfaceid,cm.id methodid,cm.methodabbreviation methodname, cm.methodname methodename,c.interfacename,'01' interfacetype " +
            " from customizationinterface c inner join customizationinterface_method cm on c.id = cm.interfaceid " +
            " where cm.id = #{interfaceId,jdbcType=VARCHAR} ")
    Map<String, String> getHJInterfaceById(@Param("interfaceId") String interfaceId); // 根据接口ID查询customizationinterface_method接口信息
}
