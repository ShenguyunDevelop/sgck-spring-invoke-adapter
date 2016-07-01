package com.sgck.springAdapter;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.sgck.common.log.DSLogger;
import com.sgck.core.exception.DSException;
import com.sgck.core.rpc.server.json.model.FuncCallInfo;
import com.sgck.core.rpc.server.InvokeError;
import com.sgck.core.rpc.server.InvokeResult;
import com.sgck.core.rpc.server.RecvHandlerCallback;
import com.sgck.core.rpc.server.json.JsonRPCHandler;

public class JsonSpringRPCHandler extends JsonRPCHandler{
	private ApplicationContext applicationContext = null;
	static private ConcurrentHashMap<String, String> canonicalNameMap = new ConcurrentHashMap<String, String>();

	public JsonSpringRPCHandler(ApplicationContext context){
		applicationContext = context;
	}

	protected String canonicalName(String handlerDomainName){
		if(handlerDomainName == null || handlerDomainName.length() <= 1){
			return handlerDomainName;
		}

		String canonicalName = null;
		if((canonicalName = (String)canonicalNameMap.get(handlerDomainName)) != null){
			return canonicalName;
		}

		canonicalName = Introspector.decapitalize(handlerDomainName);
		/*if(Character.isUpperCase(handlerDomainName.charAt(0)) 
			&& Character.isUpperCase(handlerDomainName.charAt(1))){
			canonicalName = handlerDomainName;
		}else{
			canonicalName = handlerDomainName.substring(0,1).toLowerCase() + handlerDomainName.substring(1);
		}*/

		canonicalNameMap.put(handlerDomainName, canonicalName);

		return canonicalName;
	}

	@Override
	public Object doRPC(String className,String funcName,String paramsEncodedByJson) throws Exception{
		if(applicationContext == null){
			DSLogger.error("hanldeClientReq fatal error,Spring applicationContext is null?!");
			throw new DSException(1100, "fatal error,Spring applicationContext is null?!");
		}

		List<Object> paramList = null;

		Object object = applicationContext.getBean(canonicalName(className));
		Class<?> clazz = object.getClass();
		Method func = getMethodByName(clazz, funcName);
		if (null == func)
		{
			throw new DSException(1100, "funcName[" + funcName + "] is not exist!");
		}

		Class[] paramTypeList = func.getParameterTypes();

		if(paramTypeList.length > 0){
			paramList = JSON.parseArray(paramsEncodedByJson, paramTypeList);
		}

		return func.invoke(object, paramList != null ? paramList.toArray() : null);
	}
}
