package com.proyecto.adoptapet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/fotos_mascotas/**").permitAll()
						.requestMatchers("/uploads/**").permitAll()
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/mascota/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/mascota/registrar").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/mascota/actualizar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.DELETE, "/mascota/eliminar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.GET, "/solicitud/adoptante/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/solicitud/registrar").permitAll()
						.requestMatchers(HttpMethod.PUT, "/solicitud/aprobar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/solicitud/rechazar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/solicitud/finalizar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/solicitud/finalizar-contingencia/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/solicitud/cancelar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/solicitud/no-asistio/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/solicitud/reprogramar/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.POST, "/solicitud/acta-firmada/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.GET, "/solicitud/documento/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.GET, "/adoptante/**").hasAnyRole("ADMIN", "TRABAJADOR")
						.requestMatchers(HttpMethod.PUT, "/adoptante/desactivar/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/adoptante/activar/**").hasRole("ADMIN")
						.requestMatchers("/trabajador/**").hasRole("ADMIN")
						.requestMatchers("/reporte/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}
