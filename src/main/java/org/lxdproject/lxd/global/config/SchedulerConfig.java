package org.lxdproject.lxd.global.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@EnableSchedulerLock( // 전역적 설정
        defaultLockAtLeastFor = "PT10S", // 최소 10초는 락 유지 (중복 실행 방지)
        defaultLockAtMostFor = "PT30S" // 락은 최대 10분 유지 (예외로 작업이 오래 걸릴 경우 대비)
)
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName("shed_lock")
                        .usingDbTime()
                        .build()
        );
    }
}