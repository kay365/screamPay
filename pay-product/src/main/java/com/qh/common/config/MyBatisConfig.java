package com.qh.common.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alibaba.druid.pool.DruidDataSource;

@Configuration
@EnableConfigurationProperties(DataSourceConfig.class)
@MapperScan(basePackages = MyBatisConfig.PACKAGE,sqlSessionFactoryRef = "readSqlSessionFactory")
public class MyBatisConfig {

	public static final String PACKAGE = "com.qh.*.querydao";

	public static final String MAPPER_LOCATION = "classpath:mapperquery/**/*Mapper.xml";
	
	private Logger logger = LoggerFactory.getLogger(MyBatisConfig.class);
	
	@Autowired
	private DataSourceConfig dataSourceConfig;
	
    @Value("${spring.read-dataSource.driverClassName}")
    private String driverClassName;

    @Value("${spring.read-dataSource.url}")
    private String url;

    @Value("${spring.read-dataSource.username}")
    private String username;

    @Value("${spring.read-dataSource.password}")
    private String password;

    //初始化数据库连接
    @Bean(name="readDataSource",initMethod = "init", destroyMethod = "close") // 声明其为Bean实例
    public DataSource readDataSource(){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        //configuration
        try {
			dataSourceConfig.setDataSourceProp(dataSource);
		} catch (SQLException e) {
			logger.error("druid configuration initialization filter", e);
		}
        return dataSource;
    }


    //创建Session
    @Bean("readSqlSessionFactory")
    public SqlSessionFactory readSqlSessionFactory() throws Exception{
        final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(readDataSource());
        Resource[] resource = new PathMatchingResourcePatternResolver().getResources(MyBatisConfig.MAPPER_LOCATION);
        sqlSessionFactoryBean.setMapperLocations(resource);
        org.apache.ibatis.session.Configuration configuration =  new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.qh.**.domain");
        sqlSessionFactoryBean.setTypeHandlers(new TypeHandler<?>[]{new ArrayTypeHandler(),new JsonTypeHandler()});
        return sqlSessionFactoryBean.getObject();
    }
}