package com.example.pictgram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.example.pictgram.filter.FormAuthenticationProvider;
import com.example.pictgram.repository.UserRepository;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    protected static Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    UserDetailsService service;
    
    @Autowired
    FormAuthenticationProvider authenticationProvider;


    private static final String[] URLS = { "/css/**", "/images/**", "/scripts/**", "/h2-console/**", "/favicon.ico" };


	
    /**
    * 認証から除外する
    */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() throws Exception {
        return (web) -> web.ignoring().requestMatchers(URLS);
    }
    
    /**
    * 認証を設定する
    */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
		
        http.formLogin(login -> login					// ログインの設定記述開始
                .loginProcessingUrl("/login")			// 送信先URL
                .loginPage("/login")					// ログイン画面のURL
                .defaultSuccessUrl("/topics")			// ログイン成功時のリダイレクト先URL
                .failureUrl("/login-failure")			// ログイン失敗時のリダイレクト先URL
                .permitAll()							// ログイン画面は未ログインでもアクセス可能
        ).logout(logout -> logout						// ログアウトの設定記述開始
        		.logoutUrl("/logout")					// ログアウトをトリガーするURL
                .logoutSuccessUrl("/logout-complete")	// ログアウト成功時のリダイレクト先URL
                .clearAuthentication(true)				// ログアウト時に認証情報をクリアするように設定
                .deleteCookies("JSESSIONID")			// ログアウトの成功時に削除する Cookie の名前を指定
                .invalidateHttpSession(true)			// ログアウト時に HttpSession を無効化
                .permitAll()
    	).csrf(csrf -> csrf
    			.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    			
        ).authorizeHttpRequests(authz -> authz
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()					// CSS等の静的ファイルはログインなしでもアクセス可能
        		.requestMatchers("/login", "/logout-complete", "/users/new", "/user", "/h2-console").permitAll()	// 指定のURLにはログインなしでもアクセス可能					
                .anyRequest().authenticated()																		// 他のURLはログイン後のみアクセス可能
        ).authenticationProvider(authenticationProvider);
        return http.build();
	}

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}