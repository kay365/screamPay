package com.qh.paythird.sand.utils;


/**
 * @author pan.xl
 *
 */
public abstract class SandPayRequest<T extends SandPayResponse> {

	private SandPayRequestHead head;

	public SandPayRequestHead getHead() {
		return head;
	}
	public void setHead(SandPayRequestHead head) {
		this.head = head;
	}

	public abstract Class<T> getResponseClass();
	
	public abstract String getTxnDesc();
	
}
