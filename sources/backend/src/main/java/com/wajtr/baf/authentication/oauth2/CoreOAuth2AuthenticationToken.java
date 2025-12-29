package com.wajtr.baf.authentication.oauth2;

import com.wajtr.baf.authentication.AuthenticatedTenant;
import com.wajtr.baf.user.User;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

public class CoreOAuth2AuthenticationToken extends OAuth2AuthenticationToken {
    private final User user;
    private final AuthenticatedTenant tenant;

    public CoreOAuth2AuthenticationToken(OAuth2User principal,
                                         Collection<? extends GrantedAuthority> authorities,
                                         String authorizedClientRegistrationId,
                                         User user,
                                         AuthenticatedTenant tenant) {
        super(principal, authorities, authorizedClientRegistrationId);
        this.user = user;
        this.tenant = tenant;
    }

    public @NonNull AuthenticatedTenant getTenant() {
        return tenant;
    }

    public @NonNull User getUser() {
        return user;
    }
}
