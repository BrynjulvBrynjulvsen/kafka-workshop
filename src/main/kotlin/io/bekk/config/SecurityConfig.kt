package io.bekk.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    // Allowing everything. Not for any kind of production use, obviously.
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests{
            customizer -> customizer.anyRequest().permitAll()
        }
        http.cors{
            customizer -> customizer.disable()
        }
        return http.build()
    }
}
