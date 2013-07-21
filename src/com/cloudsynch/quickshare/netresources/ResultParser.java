package com.cloudsynch.quickshare.netresources;

public abstract class ResultParser<T> {

	public abstract void parse(String content);

	public abstract Result<T> getResult();

}
