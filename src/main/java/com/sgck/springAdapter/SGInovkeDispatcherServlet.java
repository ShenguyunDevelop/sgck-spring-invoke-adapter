package com.sgck.springAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sgck.core.platform.ServletProvider;
import com.sgck.core.rpc.server.RecvHandler;

@SuppressWarnings("serial")
public class SGInovkeDispatcherServlet extends HttpServlet implements
		ServletProvider, ApplicationContextAware {

	private ApplicationContext applicationContext = null;
	private String uri = null;
	private DataPacketType dataPacketType = DataPacketType.AMF;
	private String initParams = null;
	private String contextConfigLocation;
	private HandleCallback hcallback = new HandleCallback();
//	private RecvHandler handler = null;
	private boolean isServerRequestHandler = false;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public String getURI() {
		if(uri.startsWith("/")){
			return uri;
		}else{
			return "/" + uri;
		}
	}

	@Override
	public Map<String, String> getInitParams() {
		if(initParams == null){
			return null;
		}
		
		String[] params = initParams.split(",");
		if(params.length == 0){
			return null;
		}
		
		Map<String, String> cookedParams = new HashMap<String,String>();
		for(String param : params){
			String[] nameValue = param.split("=");
			if(nameValue.length != 2){
				continue;
			}
			cookedParams.put(nameValue[0], nameValue[1]);
		}
		
		return cookedParams;
	}

	public void setDataPacketType(String dataPacketType) {
		if(dataPacketType == null || dataPacketType.isEmpty()){
			this.dataPacketType = DataPacketType.AMF;
			return;
		}
		
		if(dataPacketType.equalsIgnoreCase("json")){
			this.dataPacketType = DataPacketType.JSON;
		}else{
			this.dataPacketType = DataPacketType.AMF;
		}
	}

	/**
	 * Return the explicit context config location, if any.
	 */
	public String getDataPacketType() {
		return this.dataPacketType.toString();
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setInitParams(String initParams) {
		this.initParams = initParams;
	}

	public String getContextConfigLocation() {
		return contextConfigLocation;
	}

	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}
	
	@Override
	public void init() throws ServletException
	{
		super.init();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	        throws ServletException, IOException{
		doPost(req,resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		        throws ServletException, IOException{
		int contentLen = req.getContentLength();
		if(contentLen <= 0){
			denyIllegalRequestGently(resp);
			return;
		}
		
		RecvHandler handler = null;
		if(dataPacketType == DataPacketType.AMF){
			handler = new AmfSpringRPCHandler(applicationContext);
		}else{
			handler = new JsonSpringRPCHandler(applicationContext);
		}
		handler.setServerRequestHandler(isServerRequestHandler);
		
		if(dataPacketType == DataPacketType.JSON){
			resp.setContentType("application/json;charset=UTF-8");
		}
		handler.process(req.getInputStream(), contentLen, resp.getOutputStream(), hcallback);
	}
	
	private void denyIllegalRequestGently(HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
		response.setHeader("Pragma", "No-cache");
		response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache");

		PrintWriter out = response.getWriter();
		out.println("<head><title>Illegal Request</title></head>");
		out.println("<body>");
		out.println("<p>request data is empty?!");
		out.println("<p>we strongly advise you to use POST not GET");
		out.println("</body></html>");
		out.flush();
	}

	public boolean isServerRequestHandler() {
		return isServerRequestHandler;
	}

	public void setServerRequestHandler(boolean isServerRequestHandler) {
		this.isServerRequestHandler = isServerRequestHandler;
	}
}
