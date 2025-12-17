package com.jangada.RADAR.config;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Value("${DB_URL:jdbc:postgresql://localhost:5432/radar}")
    private String dbUrl;

    @Value("${DB_USERNAME:postgres}")
    private String dbUsername;

    @Value("${DB_PASSWORD:postgres}")
    private String dbPassword;

    @Value("${DB_MAX_POOL_SIZE:10}")
    private int maxPoolSize;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Using standard JDBC DriverManagerDataSource (no pooling)
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
}
