package com.sgck.springAdapter;

import com.sgck.core.exception.DSException;

public interface InvokeFilterService {
	public Integer checkToken(String token) throws DSException;
}
