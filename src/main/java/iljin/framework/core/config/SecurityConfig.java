package iljin.framework.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import static com.rathontech.sso.sp.config.Env.IDPM_DOMAIN_CONTEXT;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 31536000)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan("iljin.framework.core.security")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;
    private final Environment environment;

    @Autowired
    public SecurityConfig(DataSource dataSource, Environment environment) {
        super();
        this.environment = environment;
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        this.dataSource = dataSource;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery(
                        " SELECT login_id, login_pw, enable_flag " +
                        " FROM A_USER " +
                        " WHERE login_id = ? "
                )
                .authoritiesByUsernameQuery(
                        " SELECT A_USER.login_id, A_USER_ROLE.role " +
                        " FROM A_USER_ROLE " +
                        " INNER JOIN A_USER " +
                        " ON (A_USER.id = A_USER_ROLE.user_id) " +
                        " WHERE A_USER.LOGIN_ID = ? "
                )
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new CookieHttpSessionIdResolver();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/login", "/logout").permitAll()
                //.antMatchers("/login/sso", "/logout/sso").permitAll()
                .antMatchers("/login/ssoRathon").permitAll()
                .antMatchers("/dispatch/*").permitAll()
                .antMatchers("/dispatch/**").permitAll()
                .antMatchers(IDPM_DOMAIN_CONTEXT).permitAll()
                .antMatchers("/api/v1/user*/**").permitAll()
                .antMatchers("/api/v1/download2*/**", "/home", "/login/**").permitAll()
                .antMatchers("/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            .and()
                .logout().logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
            .and()
                .headers().frameOptions().disable().addHeaderWriter(new StaticHeadersWriter("X-FRAME-OPTIONS", "ALLOW-FROM http://" + environment.getProperty("server.domain-name") + "/"));

    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    public static void main(String [] args) {
    	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    	String str = encoder.encode("1111");
    	System.out.println(str);
    }
}
