package stirling.software.SPDF.config.security.saml;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SAMLUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("This method is not supported for SAML authentication");
    }

    public UserDetails loadUserBySAML(Saml2Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        String username = principal.getName();
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(principal);

        return new org.springframework.security.core.userdetails.User(username, "", authorities);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Saml2AuthenticatedPrincipal principal) {
        List<String> roles = principal.getAttribute("roles");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
