package com.ibra.support.authserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@EnableResourceServer
@SpringBootApplication
@EnableAuthorizationServer
public class AuthserverApplication {

	@RequestMapping("/user")
	public Principal user(Principal user) {
		return user;
	}
//	@Autowired
//	private UserDetailsService userDetailsService;
//	public static ApplicationContext application;
//	@Bean
//	public PasswordEncoder passwordEncoder() {
//		return new StandardPasswordEncoder();
//	}

	public static void main(String[] args) {
		SpringApplication.run(AuthserverApplication.class, args);
	}
//	@Autowired
//	public void init(AuthenticationManagerBuilder auth) throws Exception {
//		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//	}
//	@Configuration
//	protected  class AuthenticationManagerConfiguration extends GlobalAuthenticationConfigurerAdapter {
//
//		@Override
//		public void init(AuthenticationManagerBuilder auth) throws Exception {
//			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//		}
//
//		}
//	@Configuration
//	@EnableResourceServer
//	protected static class ResourceServer extends ResourceServerConfigurerAdapter {
//
//
//
//		@Override
//		public void configure(HttpSecurity http) throws Exception {
//			http.authorizeRequests().anyRequest().authenticated();
//		}
//TypeNotPresentExceptionProxy
//	}
	@Configuration
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager);
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.inMemory()
					.withClient("ibra")
					.secret("ibrasecret")
					.authorizedGrantTypes("authorization_code", "refresh_token", "implicit", "password", "client_credentials")
					.scopes("trust");
		}
	}


}

