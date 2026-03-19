package com.example.taskmanager.security;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OidcUserRequest, OidcUser> delegate = new OidcUserService();
        OidcUser oAuth2User = delegate.loadUser(userRequest);

        String sub = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("cognito:username");

        if (username == null) {
            username = email != null ? email.split("@")[0] : "user_" + sub.substring(0, 8);
        }

        Optional<AppUser> userOptional = appUserRepository.findByCognitoSub(sub);

        AppUser appUser;
        if (userOptional.isPresent()) {
            appUser = userOptional.get();
            // Update email or username if changed in Cognito
            appUser.setEmail(email);
            appUser.setUsername(username);
        } else {
            appUser = AppUser.builder()
                    .cognitoSub(sub)
                    .email(email)
                    .username(username)
                    .build();
        }
        appUserRepository.save(appUser);

        return oAuth2User;
    }
}
