package com.qh.common.domain;

import java.io.Serializable;

public class UnionPayDO implements Serializable{
    /**
	 */
	private static final long serialVersionUID = 1L;
	private String bankCode;
    private String cityId;
    private String bankBranch;
    private String unionPayNo;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getUnionPayNo() {
        return unionPayNo;
    }

    public void setUnionPayNo(String unionPayNo) {
        this.unionPayNo = unionPayNo;
    }
}
