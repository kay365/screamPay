package com.qh.system.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserDO implements Serializable {
    /**
	 */
	private static final long serialVersionUID = 1L;
	//
    private Integer userId;
    // 用户名
    private String username;
    // 用户真实姓名
    private String name;
    // 密码
    private String password;
    // 资金密码
    private String fundPassword;
    // 部门
    private Integer deptId;
    private String deptName;
    // 邮箱
    private String email;
    // 手机号
    private String mobile;
    // 状态 0:禁用，1:正常
    private Integer status;
    // 创建用户id
    private Integer userIdCreate;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
    //角色
    private List<Integer> roleIds;
    //用户类型
    private Integer userType;
    /**
     * 设置：
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取：
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置：用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取：用户名
     */
    public String getUsername() {
        return username;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置：密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取：密码
     */
    public String getPassword() {
        return password;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    /**
     * 设置：邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取：邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置：手机号
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * 获取：手机号
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * 设置：状态 0:禁用，1:正常
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取：状态 0:禁用，1:正常
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置：创建用户id
     */
    public void setUserIdCreate(Integer userIdCreate) {
        this.userIdCreate = userIdCreate;
    }

    /**
     * 获取：创建用户id
     */
    public Integer getUserIdCreate() {
        return userIdCreate;
    }

    /**
     * 设置：创建时间
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 获取：创建时间
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 设置：修改时间
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * 获取：修改时间
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    public String getFundPassword() {
        return fundPassword;
    }

    public void setFundPassword(String fundPassword) {
        this.fundPassword = fundPassword;
    }

    @Override
    public String toString() {
        return "UserDO{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", fundPassword='" + fundPassword + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", status=" + status +
                ", userIdCreate=" + userIdCreate +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", roleIds=" + roleIds +
                '}';
    }

	public List<Integer> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Integer> roleIds) {
		this.roleIds = roleIds;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}
    
}
