package com.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 序列化工具
 * 
 * @author 王威峰
 */
public class SerializeTools
{
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public synchronized static final SerializeTools get()
	{
		return new SerializeTools();
	}

	static
	{
		MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	/**
	 * 对象序列化为JSON
	 * 
	 * @param object
	 * @return
	 * @throws JsonProcessingException
	 */
	public static synchronized final String obj2Json(Object object) throws Exception
	{
		return MAPPER.writeValueAsString(object);
	}

	/**
	 * JSON反序列化为对象
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static synchronized final <T> T json2Obj(Class<T> clazz, String json) throws Exception
	{
		return MAPPER.readValue(json, clazz);
	}

	/**
	 * 反序列化
	 * 
	 * @param <T>
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public synchronized final <T> List<T> json2List(Class<T> clazz, Collection<String> bytes) throws Exception
	{
		ArrayList<T> datas = new ArrayList<>();
		bytes.forEach(str -> {
			try
			{
				datas.add(MAPPER.readValue(str, clazz));
			} catch (Exception e)
			{
				System.err.println("Java8 JSON读取异常"+ e);
			}
		});
		return datas;
	}
	
	public static String sereplece(String content) {
		content = content.replace("&lt;", "<");
		content = content.replace("&gt;", ">");
		content = content.replace("&quot;", "'");
		content = content.replace("&nbsp;", " ");
		content = content.replace("&#x3D;", "=");
		return content;
	}
	
}