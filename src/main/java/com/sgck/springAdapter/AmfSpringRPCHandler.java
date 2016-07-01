package com.sgck.springAdapter;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.sgck.common.log.DSLogger;
import com.sgck.core.exception.DSException;
import com.sgck.core.rpc.server.InvokeError;
import com.sgck.core.rpc.server.InvokeResult;
import com.sgck.core.rpc.server.RecvHandler;
import com.sgck.core.rpc.server.RecvHandlerCallback;
import com.sgck.core.rpc.server.amf.AmfRPCHandler;

import flex.messaging.io.amf.ASObject;

public class AmfSpringRPCHandler extends AmfRPCHandler {
	private ApplicationContext applicationContext = null;
	static private ConcurrentHashMap<String, String> canonicalNameMap = new ConcurrentHashMap<String, String>();

	public AmfSpringRPCHandler(ApplicationContext context){
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
	protected Integer checkToken(Object tokenObj) throws DSException {
		String token = tokenObj.toString();
		//if(!token.toString().equals(SuperTokenConst.SUPERTOKEN)){
			try{
				InvokeFilterService redisService = this.applicationContext.getBean(InvokeFilterService.class);
				if(null != redisService){
					return redisService.checkToken(token);
				}
				return null;
			}catch(BeansException e){
				DSLogger.info("not found InvokeFilterService,ingore it.....");
				return null;
			}
		//}
	}

	@Override
	protected Object doRPC(String className,String funcName,ArrayList<Object> paramList) throws Exception{
		if(applicationContext == null){
			DSLogger.error("hanldeClientReq fatal error,Spring applicationContext is null?!");
			throw new DSException(1100, "fatal error,Spring applicationContext is null?!");
		}

		Object object = applicationContext.getBean(canonicalName(className));
		Class<?> clazz = object.getClass();
		Method func = getMethodByName(clazz, funcName);
		if (null == func)
		{
			throw new DSException(1100, "funcName[" + funcName + "] is not exist!");
		}

		Class[] paramTypeList = func.getParameterTypes();
		if (paramList != null && paramList.size() != paramTypeList.length)
		{
			throw new DSException(1101, "func need " + paramTypeList.length + "params, argument count is mismatch");
		}

		if (paramList != null)
		{
			for (int index = 0; index < paramList.size(); index++)
			{
				paramList.set(index, typeConvert(paramList.get(index), paramTypeList[index]));
			}
		}

		return func.invoke(object, paramList != null ? paramList.toArray() : null);
	}
}
