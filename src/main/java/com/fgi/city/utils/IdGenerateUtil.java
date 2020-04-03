package com.fgi.city.utils;


/**
 * 主键生成工具类
 * @author  sirc_fxf E-mail: 
 * @date 创建时间：2016年3月18日 上午11:08:31 
 * @version 1.0 
 *
 */
public class IdGenerateUtil {

	/**
	 * 生成32位UNID，字母大写
	 * @return String
	 */
	public static String getKey() {
		String unid = "";
		unid = java.util.UUID.randomUUID().toString();
		unid = unid.replaceAll("-", "").toUpperCase();
		return unid;
	}
	public static void main(String[] args) {
		System.out.println(	getKey());
	}
	
}
