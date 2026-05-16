package com.ecommerce.security.handler;

import com.ecommerce.entity.Users;
import com.ecommerce.util.JwtUtil;
import com.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;   // ✅ Added

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // ✅ Get info from Google
        String email    = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");
        String picture  = oAuth2User.getAttribute("picture");

        // ✅ Check if user exists in DB
        Users user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // ✅ New Google user — save to DB as CUSTOMER
            Users newUser = Users.builder()
                    .fullName(name)
                    .email(email)
                    .passwordHash("")        // no password for OAuth users
                    .role("CUSTOMER")        // default role
                    .mobile("")
                    .build();

            userRepository.save(newUser);
            user = newUser;
        }

        // ✅ Generate JWT with user's role from DB
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String encodedName = java.net.URLEncoder.encode(
                user.getFullName(), "UTF-8"
        );
        String encodedEmail = java.net.URLEncoder.encode(
                user.getEmail(), "UTF-8"
        );


        // ✅ Redirect to Angular with token
        String redirectUrl = frontendUrl + "/oauth2/callback?token="
                + jwt
                + "&role=" + user.getRole()
                + "&name=" + encodedName
                + "&email=" + encodedEmail;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
