package com.sgck.springAdapter;
import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 此类应该禁止使用
 * 因为所有的组件或对象都应该是Spring-powered的，所有的bean对象应该是自动注入的
 */
public class SpringBeanUtil  {
    
	private static ServletContext sc = null;
	
	public static void setServletContext(ServletContext sc){
		SpringBeanUtil.sc = sc;
	}
	
	public static ServletContext getServletContext(){
		return sc;
	}
	
	public static ApplicationContext getApplicationContext(String subSystemName){
		if (SpringBeanUtil.sc == null){
    		throw new RuntimeException("请在系统启动时加载InitializeServlet");
    	}
    	
    	return WebApplicationContextUtils.getWebApplicationContext(SpringBeanUtil.getServletContext(),subSystemName);
	}
	
    public static Object getBean(String subSystemName,String beanName){
    	if (SpringBeanUtil.sc == null){
    		throw new RuntimeException("请在系统启动时加载InitializeServlet");
    	}
    	
    	ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(SpringBeanUtil.getServletContext(),subSystemName);
    	return context.getBean(beanName);
    }
    
    public static String getContextName(){
    	return null;
    }
    
    public static <T> T getBean(String subSystemName,Class<T> clazz) {
    	if (SpringBeanUtil.sc == null){
    		throw new RuntimeException("请在系统启动时加载InitializeServlet");
    	}
    	
    	ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(SpringBeanUtil.getServletContext(),subSystemName);
    	return context.getBean(clazz);
    }
}