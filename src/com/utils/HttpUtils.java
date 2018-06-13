package com.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 外部请求工具
 * 
 * @author 王威峰
 */
public class HttpUtils
{
	private static final SerializeTools	SERIALIZE_TOOLS	= SerializeTools.get();
	protected final static ObjectMapper	mapper			= new ObjectMapper();

	/**
	 * 发起普通请求
	 * 
	 * @param callback
	 */
	public static final String requestByHttps(String urlstr, byte[] data) throws Exception
	{
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType)
			{
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType)
			{
			}
		} };
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new SecureRandom());
		HostnameVerifier hv = new HostnameVerifier()
		{
			public boolean verify(String urlHostName, SSLSession session)
			{
				return urlHostName.equals(session.getPeerHost());
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		URL url = new URL(urlstr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setReadTimeout(3000);
		conn.setDoInput(true);
		if (data != null && data.length > 0)
		{
			conn.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(data);
			out.flush();
			out.close();
		}
		conn.connect();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
		{
			throw new Exception("response code is : " + conn.getResponseCode());
		}
		String result = IOUtils.toString(conn.getInputStream(), "utf-8");
		if (result == null)
		{
			throw new Exception("response is null");
		}
		conn.disconnect();
		return result;
	}

	/**
	 * 发起普通请求
	 * 
	 * @param callback
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public static final Map<String, Object> requestByHttpForJson(String urlstr, byte[] data) throws Exception
	{
		System.out.println("Request data : " + new String(data));
		URL url = new URL(urlstr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setReadTimeout(3000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();
		OutputStream out = conn.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
		String result = null;
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
		{
			result = IOUtils.toString(conn.getErrorStream(), "utf-8");
		} else
		{
			result = IOUtils.toString(conn.getInputStream(), "utf-8");
		}
		System.out.println("Response data : " + result);
		conn.disconnect();
		return SERIALIZE_TOOLS.json2Obj(Map.class, result);
	}


	public static class HttpModel
	{
		private String				url;
		private byte[]				postData;
		private Map<String, String>	headers;
		private String				method;
		private String				charset;
		private boolean				isHttps;
		private Map<String, String>	getData;

		public HttpModel()
		{
			super();
			this.url = null;
			this.method = null;
			this.charset = "UTF-8";
			this.headers = null;
			this.postData = null;
			this.isHttps = false;
			this.getData = null;
		}

		public HttpModel(String method, boolean isHttps, String url)
		{
			super();
			this.url = url;
			this.method = method;
			this.charset = "UTF-8";
			this.headers = null;
			this.postData = null;
			this.isHttps = isHttps;
			this.getData = null;
		}

		public String getUrl()
		{
			return url;
		}

		public HttpModel setUrl(String url)
		{
			this.url = url;
			return this;
		}

		public byte[] getPostData()
		{
			return postData;
		}

		public HttpModel setPostData(byte[] postData)
		{
			this.postData = postData;
			return this;
		}

		public String getMethod()
		{
			return method;
		}

		public HttpModel setMethod(String method)
		{
			this.method = method;
			return this;
		}

		public String getCharset()
		{
			return charset;
		}

		public HttpModel setCharset(String charset)
		{
			this.charset = charset;
			return this;
		}

		public boolean isHttps()
		{
			return isHttps;
		}

		public HttpModel setHttps(boolean isHttps)
		{
			this.isHttps = isHttps;
			return this;
		}

		public Map<String, String> getHeaders()
		{
			return headers;
		}

		public Map<String, String> getGetData()
		{
			return getData;
		}

		public HttpModel addHeader(String key, String val)
		{
			if (headers == null)
			{
				headers = new LinkedHashMap<>();
			}
			headers.put(key, val);
			return this;
		}

		public HttpModel addQuery(String key, String val)
		{
			if (getData == null)
			{
				getData = new LinkedHashMap<>();
			}
			getData.put(key, val);
			return this;
		}

		@Override
		public String toString() {
			return "HttpModel [url=" + url + ", postData=" + Arrays.toString(postData) + ", headers=" + headers
					+ ", method=" + method + ", charset=" + charset + ", isHttps=" + isHttps + ", getData=" + getData
					+ "]";
		}
	}

	public static String request(HttpModel httpModel) throws Exception
	{
		HttpURLConnection conn = null;
		try
		{
			if (httpModel.getUrl() == null)
			{
				throw new NullPointerException("url is null");
			}
			if (httpModel.getMethod() == null)
			{
				throw new NullPointerException("method is null");
			}
			if (httpModel.getCharset() == null)
			{
				throw new NullPointerException("charset is null");
			}
			if (httpModel.isHttps())
			{
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
				{
					public X509Certificate[] getAcceptedIssuers()
					{
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType)
					{
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType)
					{
					}
				} };
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new SecureRandom());
				HostnameVerifier hv = new HostnameVerifier()
				{
					public boolean verify(String urlHostName, SSLSession session)
					{
						return urlHostName.equals(session.getPeerHost());
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			}
			String reqUrl = httpModel.getUrl();
			Map<String, String> queryData = httpModel.getGetData();
			if (queryData != null && queryData.size() > 0)
			{
				reqUrl += "?";
				for (Entry<String, String> entry : queryData.entrySet())
				{
					reqUrl += entry.getKey() + "=" + entry.getValue() + "&";
				}
				if (reqUrl.endsWith("&")) reqUrl = reqUrl.substring(0, reqUrl.length() - 1);
			}
			System.out.println("发起请求->" + reqUrl);
			URL url = new URL(reqUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(3000);
			conn.setRequestMethod(httpModel.getMethod());
			conn.setDoInput(true);
			if (httpModel.getHeaders() != null && httpModel.getHeaders().size() > 0)
			{
				Map<String, String> headers = httpModel.getHeaders();
				for (String header_key : headers.keySet())
				{
					conn.setRequestProperty(header_key, headers.get(header_key));
				}
			}
			if (httpModel.getMethod().equals("POST") && httpModel.getPostData() != null)
			{
				conn.setDoOutput(true);
				OutputStream out = conn.getOutputStream();
				out.write(httpModel.getPostData());
				out.flush();
				out.close();
			}
			conn.connect();
			String result = IOUtils.toString(conn.getInputStream(), httpModel.getCharset());
			System.out.println("响应数据->" + result);
			return result;
		} finally
		{
			if (conn != null) conn.disconnect();
		}
	}

	public static byte[] requestForRaw(HttpModel httpModel) throws Exception
	{
		HttpURLConnection conn = null;
		try
		{
			if (httpModel.getUrl() == null)
			{
				throw new NullPointerException("url is null");
			}
			if (httpModel.getMethod() == null)
			{
				throw new NullPointerException("method is null");
			}
			if (httpModel.getCharset() == null)
			{
				throw new NullPointerException("charset is null");
			}
			if (httpModel.isHttps())
			{
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
				{
					public X509Certificate[] getAcceptedIssuers()
					{
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType)
					{
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType)
					{
					}
				} };
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new SecureRandom());
				HostnameVerifier hv = new HostnameVerifier()
				{
					public boolean verify(String urlHostName, SSLSession session)
					{
						return urlHostName.equals(session.getPeerHost());
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			}
			String reqUrl = httpModel.getUrl();
			Map<String, String> queryData = httpModel.getGetData();
			if (queryData != null && queryData.size() > 0)
			{
				reqUrl += "?";
				for (Entry<String, String> entry : queryData.entrySet())
				{
					reqUrl += entry.getKey() + "=" + entry.getValue() + "&";
				}
				if (reqUrl.endsWith("&")) reqUrl = reqUrl.substring(0, reqUrl.length() - 1);
			}
			System.out.println("发起请求->" + reqUrl);
			URL url = new URL(reqUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(3000);
			conn.setUseCaches(false);
			conn.setRequestMethod(httpModel.getMethod());
			conn.setDoInput(true);
			if (httpModel.getHeaders() != null && httpModel.getHeaders().size() > 0)
			{
				Map<String, String> headers = httpModel.getHeaders();
				for (String header_key : headers.keySet())
				{
					conn.setRequestProperty(header_key, headers.get(header_key));
				}
			}
			if (httpModel.getMethod().equals("POST") && httpModel.getPostData() != null)
			{
				conn.setDoOutput(true);
				OutputStream out = conn.getOutputStream();
				out.write(httpModel.getPostData());
				out.flush();
				out.close();
			}
			conn.connect();
			if (conn.getResponseCode() == 200)
			{
				InputStream inputStream = conn.getInputStream();
				System.out.println("响应数据->" + conn.getResponseCode());
				byte[] datas = IOUtils.toByteArray(inputStream);
				inputStream.close();
				return datas;
			} else
			{
				System.out.println("响应数据->" + null);
				return null;
			}
		} finally
		{
			if (conn != null) conn.disconnect();
		}
	}

	public static <T> T requestForJson(Class<T> clazz, HttpModel httpModel) throws Exception
	{
		String result = request(httpModel);
		if (result != null)
		{
			return mapper.readValue(result, clazz);
		}
		return null;
	}

    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");//**注意点1**，需要此格式，后边这个字符集可以不设置
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
//          in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
    
    public static String sendGetUtf(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
    
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");//**注意点1**，需要此格式，后边这个字符集可以不设置
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }    

}
