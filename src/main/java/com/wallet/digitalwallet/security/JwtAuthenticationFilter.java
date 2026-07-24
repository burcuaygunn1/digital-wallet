package com.wallet.digitalwallet.security;

import com.wallet.digitalwallet.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Başlık yoksa veya "Bearer " ile başlamıyorsa zincire devam et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userOptional = userRepository.findByEmail(userEmail);

                if (userOptional.isPresent()) {
                    // Kullanıcı veritabanında var, token geçerli mi kontrol et
                    if (jwtService.isTokenValid(jwt, userEmail)) {
                        UserDetails userDetails = new User(userEmail, "", Collections.emptyList());

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Kullanıcıyı Spring Security Oturumuna (Context) Yerleştir
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.err.println("JWT Hatasi: Token gecerli degil (isTokenValid false dondu).");
                    }
                } else {
                    System.err.println("JWT Hatasi: Token icindeki e-posta veritabaninda bulunamadi -> " + userEmail);
                }
            }
        } catch (Exception e) {
            System.err.println("JWT Dogrulama Istisnasi: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}