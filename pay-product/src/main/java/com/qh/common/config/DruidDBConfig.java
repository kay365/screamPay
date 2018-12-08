package com.qh.common.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 */
@Configuration
@EnableConfigurationProperties(DataSourceConfig.class)
@MapperScan(basePackages = DruidDBConfig.PACKAGE, sqlSessionFactoryRef = "sqlSessionFactory")
public class DruidDBConfig {

	public static final String PACKAGE = "com.qh.*.dao";

	public static final String MAPPER_LOCATION = "classpath:mapper/**/*Mapper.xml";

	@Autowired
	private DataSourceConfig dataSourceConfig;

	private Logger logger = LoggerFactory.getLogger(DruidDBConfig.class);

	@Bean(name="dataSource",initMethod = "init", destroyMethod = "close") // 声明其为Bean实例
	@Primary
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(dataSourceConfig.getUrl());
		dataSource.setUsername(dataSourceConfig.getUsername());
		dataSource.setPassword(dataSourceConfig.getPassword());
		dataSource.setDriverClassName(dataSourceConfig.getDriverClassName());
		// configuration
		try {
			dataSourceConfig.setDataSourceProp(dataSource);
		} catch (SQLException e) {
			logger.error("druid configuration initialization filter", e);
		}
		List<Filter> filterList=new ArrayList<>();
	    filterList.add(wallFilter());
	    dataSource.setProxyFilters(filterList);
		return dataSource;
	}
	
	@Bean
	public WallFilter wallFilter(){
	    WallFilter wallFilter=new WallFilter();
	    wallFilter.setConfig(wallConfig());
	    return  wallFilter;
	}
	@Bean
	public WallConfig wallConfig(){
	    WallConfig config =new WallConfig();
	    config.setMultiStatementAllow(true);//允许一次执行多条语句
	    config.setNoneBaseStatementAllow(true);//允许非基本语句的其他语句
	    return config;
	}
	
	
	
	
	// 数据源事务管理器
	@Bean("dataSourceTransactionManager")
	@Primary
	public DataSourceTransactionManager dataSourceTransactionManager() {
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
		dataSourceTransactionManager.setDataSource(dataSource());
		return dataSourceTransactionManager;
	}

	// 创建Session
	@Bean("sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource());
		Resource[] resource = new PathMatchingResourcePatternResolver().getResources(DruidDBConfig.MAPPER_LOCATION);
		sqlSessionFactoryBean.setMapperLocations(resource);
		org.apache.ibatis.session.Configuration configuration =  new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.qh.**.domain");
        sqlSessionFactoryBean.setTypeHandlers(new TypeHandler<?>[]{new ArrayTypeHandler(),new JsonTypeHandler()});
		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public ServletRegistrationBean druidServlet() {
		ServletRegistrationBean reg = new ServletRegistrationBean();
		reg.setServlet(new StatViewServlet());
		reg.addUrlMappings("/druid/*");
		reg.addInitParameter("allow", ""); // 白名单
		return reg;
	}

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new WebStatFilter());
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		filterRegistrationBean.addInitParameter("profileEnable", "true");
		filterRegistrationBean.addInitParameter("principalCookieName", "USER_COOKIE");
		filterRegistrationBean.addInitParameter("principalSessionName", "USER_SESSION");
		filterRegistrationBean.addInitParameter("DruidWebStatFilter", "/*");
		return filterRegistrationBean;
	}

}
