package com.qh.common.config;

import java.sql.SQLException;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @ClassName DataSourceConfig
 * @Description 数据库配置
 * @Date 2017年12月1日 上午11:02:51
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "spring.dataSource")
public class DataSourceConfig {
	private String url;

	private String username;

	private String password;

	private String driverClassName;

	private int initialSize;

	private int minIdle;

	private int maxActive;

	private int maxWait;

	private int timeBetweenEvictionRunsMillis;

	private int minEvictableIdleTimeMillis;

	private String validationQuery;

	private boolean testWhileIdle;

	private boolean testOnBorrow;

	private boolean testOnReturn;

	private boolean poolPreparedStatements;

	private int maxPoolPreparedStatementPerConnectionSize;

	private String filters;

	private String connectionProperties;

	/**
	 * @Description 设置数据库属性
	 * @param dataSource
	 * @throws SQLException
	 */
	public void setDataSourceProp(DruidDataSource dataSource) throws SQLException {
		dataSource.setInitialSize(initialSize);
		dataSource.setMinIdle(minIdle);
		dataSource.setMaxActive(maxActive);
		dataSource.setMaxWait(maxWait);
		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setTestOnBorrow(testOnBorrow);
		dataSource.setTestOnReturn(testOnReturn);
		dataSource.setPoolPreparedStatements(poolPreparedStatements);
		dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
		dataSource.setFilters(filters);
		dataSource.setConnectionProperties(connectionProperties);
	}

	public int getInitialSize() {
		return initialSize;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public int getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public int getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	public boolean isPoolPreparedStatements() {
		return poolPreparedStatements;
	}

	public int getMaxPoolPreparedStatementPerConnectionSize() {
		return maxPoolPreparedStatementPerConnectionSize;
	}

	public String getFilters() {
		return filters;
	}

	public String getConnectionProperties() {
		return connectionProperties;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		this.poolPreparedStatements = poolPreparedStatements;
	}

	public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
		this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public void setConnectionProperties(String connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

}
