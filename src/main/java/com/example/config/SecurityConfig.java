package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SecurityConfig
 * @Description:
 * @Author: 刘苏义
 * @Date: 2023年04月22日15:36
 * @Version: 1.0
 **/
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    // 授权
    protected void configure(HttpSecurity http) throws Exception {
        // 链式编程
        // 请求授权的设置
        http.authorizeRequests()
                .antMatchers("/", "/index").authenticated()
                .antMatchers("/level1/**").hasRole("VIP1")
                .antMatchers("/level2/**").hasRole("VIP2")
                .antMatchers("/level3/**").hasRole("VIP3")
                .and()
                .formLogin().loginPage("/login")
                // 登录需要的参数
                .usernameParameter("username")
                .passwordParameter("password")
                //登录请求交给 SpringSecurity
                .loginProcessingUrl("/doLogin")
                //认证成功 forward 跳转路径
                .successForwardUrl("/index")
                .and()
                .rememberMe().rememberMeParameter("remeber")
                .and()
                .csrf().disable(); //禁止csrf 跨站请求保护;
    }

    @Override
    // 认证
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // auth.jdbcAuthentication() 从数据库获取认证信息
        // 现在没有数据库，只能从内存中获取认证信息
        auth.inMemoryAuthentication()
                .passwordEncoder(new BCryptPasswordEncoder()).withUser("admin").password(new BCryptPasswordEncoder().encode("12345")).roles("VIP1")
                .and()
                .passwordEncoder(new BCryptPasswordEncoder()).withUser("liusuyi").password(new BCryptPasswordEncoder().encode("12345")).roles("VIP1","VIP2")
                .and()
                .passwordEncoder(new BCryptPasswordEncoder()).withUser("dingli").password(new BCryptPasswordEncoder().encode("12345")).roles("VIP1","VIP2","VIP3");
    }

}
