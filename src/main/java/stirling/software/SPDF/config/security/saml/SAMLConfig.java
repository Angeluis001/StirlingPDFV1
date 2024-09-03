package stirling.software.SPDF.config.security.saml;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutResponseRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutSuccessHandler;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutSuccessHandler;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestValidator;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestValidator;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseValidator;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutResponseValidator;

import stirling.software.SPDF.model.ApplicationProperties;

@Configuration
public class SAMLConfig {

    private final ApplicationProperties applicationProperties;

    public SAMLConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        RelyingPartyRegistration registration = RelyingPartyRegistration
                .withRegistrationId("saml")
                .entityId(applicationProperties.getSecurity().getSAML().getEntityId())
                .assertionConsumerServiceLocation(applicationProperties.getSecurity().getSAML().getSpBaseUrl() + "/saml2/acs")
                .singleLogoutServiceLocation(applicationProperties.getSecurity().getSAML().getSpBaseUrl() + "/saml2/logout")
                .idpWebSsoUrl(applicationProperties.getSecurity().getSAML().getIdpMetadataLocation())
                .build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    @Bean
    public Saml2LogoutRequestRepository logoutRequestRepository() {
        return new OpenSaml4LogoutRequestRepository();
    }

    @Bean
    public Saml2LogoutResponseRepository logoutResponseRepository() {
        return new OpenSaml4LogoutResponseRepository();
    }

    @Bean
    public Saml2LogoutSuccessHandler logoutSuccessHandler() {
        return new OpenSaml4LogoutSuccessHandler();
    }

    @Bean
    public Saml2LogoutRequestValidator logoutRequestValidator() {
        return new OpenSaml4LogoutRequestValidator();
    }

    @Bean
    public Saml2LogoutResponseValidator logoutResponseValidator() {
        return new OpenSaml4LogoutResponseValidator();
    }
}
