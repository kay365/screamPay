package com.qh.common.domain;

import java.util.List;

/**
 * 表数据
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月20日 上午12:02:55
 */
public class TableDO {
    //表的名称
    private String tableName;
    //表的备注
    private String tableComment;
	private String createTime;
	private String engine;
    //表的主键
    private ColumnDO pk;
    //表的列名(不包含主键)
    private List<ColumnDO> columns;

    //类名(第一个字母大写)，如：sys_user => SysUser
    private String className;
    //类名(第一个字母小写)，如：sys_user => sysUser
    private String classname;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public ColumnDO getPk() {
        return pk;
    }

    public void setPk(ColumnDO pk) {
        this.pk = pk;
    }

    public List<ColumnDO> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDO> columns) {
        this.columns = columns;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    @Override
    public String toString() {
        return "TableDO{" +
                "tableName='" + tableName + '\'' +
                ", tableComment='" + tableComment + '\'' +
                ", pk=" + pk +
                ", columns=" + columns +
                ", className='" + className + '\'' +
                ", classname='" + classname + '\'' +
                '}';
    }

	public String getTableComment() {
		return tableComment;
	}

	public void setTableComment(String tableComment) {
		this.tableComment = tableComment;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}
    
}
