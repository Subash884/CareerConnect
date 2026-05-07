package com.careerconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Look for the "Authorization" header in the incoming request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. If there is no header, or it doesn't start with "Bearer ", let them pass
        // (They might be trying to access a public page like Login or Register)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (Remove the first 7 characters: "Bearer ")
        jwt = authHeader.substring(7);
        // Extract the email from the token using our JwtUtil
        userEmail = jwtUtil.extractUsername(jwt);

        // 4. If we found an email, and the user isn't already logged into the current security context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Ask the Receptionist (UserDetailsService) to get the user's details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. If the badge (token) is valid, open the door!
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                // Create an authentication ticket
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Hand the ticket to Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Move on to the next filter or the actual controller
        filterChain.doFilter(request, response);
    }
}