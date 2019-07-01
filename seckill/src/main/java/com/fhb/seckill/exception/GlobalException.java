package com.fhb.seckill.exception;


import com.fhb.seckill.result.CodeMsg;

/**
 * 全局异常处理
 *
 * @author hbfang
 */
public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private CodeMsg cm;
	public GlobalException(CodeMsg cm){
		super(cm.toString());
		this.cm=cm;
		
	}
	public CodeMsg getCm() {
		return cm;
	}
	public void setCm(CodeMsg cm) {
		this.cm = cm;
	}
	
}
