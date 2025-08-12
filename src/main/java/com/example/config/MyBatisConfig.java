package com.example.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis配置类
 *
 * @author xupeng
 */
@Configuration
public class MyBatisConfig {

  @Autowired private DataSource dataSource;

  @Bean
  public SqlSessionFactory sqlSessionFactory() throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);

    // 设置mapper xml文件位置
    sessionFactory.setMapperLocations(
        new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));

    // 设置类型别名包
    sessionFactory.setTypeAliasesPackage("com.example.entity");

    // 设置配置
    org.apache.ibatis.session.Configuration configuration =
        new org.apache.ibatis.session.Configuration();
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.setCacheEnabled(true);
    configuration.setLazyLoadingEnabled(true);
    sessionFactory.setConfiguration(configuration);

    return sessionFactory.getObject();
  }

  @Bean
  public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }
}
