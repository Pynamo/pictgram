package com.example.pictgram;

import com.example.pictgram.entity.SocialUser;
import com.example.pictgram.entity.User;
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
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
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


    //private static final String[] URLS = { "/css/**", "/images/**", "/scripts/**", "/h2-console/**", "/favicon.ico" };
    private static final String[] URLS = { "/css/**", "/images/**", "/scripts/**", "/h2-console/**", "/favicon.ico", "/OneSignalSDKWorker.js" };

	
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
                ).oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login-failure")
                        .permitAll()
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .oidcUserService(this.oidcUserService())
                                .userService(this.oauth2UserService())
                        )
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

    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();
        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            OAuth2AccessToken accessToken = userRequest.getAccessToken();

            log.debug("accessToken={}", accessToken);

            oidcUser = new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
            String email = oidcUser.getEmail();
            User user = repository.findByUsername(email);
            if (user == null) {
                user = new User(email, oidcUser.getFullName(), "", User.Authority.ROLE_USER);
                repository.saveAndFlush(user);
            }
            oidcUser = new SocialUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(),
                    user.getUserId());

            return oidcUser;
        };
    }

    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User oauth2User = delegate.loadUser(request);

            log.debug(oauth2User.toString());

            String name = oauth2User.getAttribute("login");
            User user = repository.findByUsername(name);
            if (user == null) {
                user = new User(name, name, "", User.Authority.ROLE_USER);
                repository.saveAndFlush(user);
            }
            SocialUser socialUser = new SocialUser(oauth2User.getAuthorities(), oauth2User.getAttributes(), "id",
                    user.getUserId());

            return socialUser;
        };
    }
}