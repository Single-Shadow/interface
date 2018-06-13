<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="com.utils.SerializeTools"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.PrintStream"%>
<%@page import="java.util.*"%>
<%@ page language="java" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>

<%
	response.setHeader("Access-Control-Allow-Origin", "*");
	response.setHeader("Content-Type", "application/x-www-form-urlencoded");
	request.setCharacterEncoding("utf-8"); //	 	req = new String(req.getBytes("iso-8859-1"), "utf-8");
	response.setContentType("text/html;charset=utf-8");

	InputStream is = request.getInputStream();
	int i = -1;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	while ((i = is.read()) != -1) {
		baos.write(i);
	}
	String req = baos.toString();
	//	String req = request.getParameter("req");
	if (req != null && req.length() != 0) {
		File f = new File("D:\\text.txt");
		FileOutputStream fs = new FileOutputStream(f, true);
		PrintStream p = new PrintStream(fs);
		p.println(req);
		p.close();
	}

	Map<String, Object> parMap = new HashMap<String, Object>();
	Map<String, Object> parMap2 = new HashMap<String, Object>();
	parMap.put("code", 0);
	parMap.put("obj", parMap2);
	parMap2.put("name", "王");
	parMap2.put("age", 26);
	response.getWriter().write(SerializeTools.obj2Json(parMap));
%>