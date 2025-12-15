package com.wajtr.baf.core.auth.token;

import com.wajtr.baf.core.auth.AuthenticatedTenant;
import com.wajtr.baf.core.auth.AuthenticatedUser;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

public class CoreOAuth2AuthenticationToken extends OAuth2AuthenticationToken {
    private final AuthenticatedUser user;
    private final AuthenticatedTenant tenant;

    public CoreOAuth2AuthenticationToken(OAuth2User principal,
                                         Collection<? extends GrantedAuthority> authorities,
                                         String authorizedClientRegistrationId,
                                         AuthenticatedUser user,
                                         AuthenticatedTenant tenant) {
        super(principal, authorities, authorizedClientRegistrationId);
        this.user = user;
        this.tenant = tenant;
    }

    public @NonNull AuthenticatedTenant getTenant() {
        return tenant;
    }

    public @NonNull AuthenticatedUser getUser() {
        return user;
    }
}
