package com.utc.nda.request.dtos;

public class SearchData {

	private String item;

	private long count;

	public SearchData(String s, Integer count) {
		this.item = s;
		this.count = count;		
	}
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}


}