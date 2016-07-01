package com.sgck.springAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import com.sgck.common.log.DSLogger;
import com.sgck.core.rpc.server.InvokeError;
import com.sgck.core.rpc.server.InvokeResult;
import com.sgck.core.rpc.server.RecvHandlerCallback;

public class HandleCallback extends RecvHandlerCallback{
		@Override
		public void onOK(InvokeResult reqObj){
		}
	
		
		@Override
		public void onError(InvokeError reqObj, Object invokedObject,Exception e){
			try{
				if(e instanceof InvocationTargetException){
					String className = reqObj.getClassName();
					String funcName = reqObj.getFuncName();
					DSLogger.error("failed to invoke " + className + ":" + funcName + ",because : ",e );
				}
			}catch(Exception err){
				DSLogger.error("HandleCallback:onError",err);
			}
		}
}