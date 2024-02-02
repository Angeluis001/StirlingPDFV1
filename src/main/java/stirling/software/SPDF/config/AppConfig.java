package stirling.software.SPDF.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import stirling.software.SPDF.model.ApplicationProperties;

@Configuration
public class AppConfig {

    @Autowired ApplicationProperties applicationProperties;

    @Bean(name = "loginEnabled")
    public boolean loginEnabled() {
        return applicationProperties.getSecurity().getEnableLogin();
    }

    @Bean(name = "appName")
    public String appName() {
        String homeTitle = applicationProperties.getUi().getAppName();
        return (homeTitle != null) ? homeTitle : "Stirling PDF";
    }

    @Bean(name = "appVersion")
    public String appVersion() {
        Resource resource = new ClassPathResource("version.properties");
        Properties props = new Properties();
        try {
            props.load(resource.getInputStream());
            return props.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }

    @Bean(name = "homeText")
    public String homeText() {
        return (applicationProperties.getUi().getHomeDescription() != null)
                ? applicationProperties.getUi().getHomeDescription()
                : "null";
    }

    @Bean(name = "navBarText")
    public String navBarText() {
        String defaultNavBar =
                applicationProperties.getUi().getAppNameNavbar() != null
                        ? applicationProperties.getUi().getAppNameNavbar()
                        : applicationProperties.getUi().getAppName();
        return (defaultNavBar != null) ? defaultNavBar : "Stirling PDF";
    }

    @Bean(name = "enableAlphaFunctionality")
    public boolean enableAlphaFunctionality() {
        return applicationProperties.getSystem().getEnableAlphaFunctionality() != null
                ? applicationProperties.getSystem().getEnableAlphaFunctionality()
                : false;
    }

    @Bean(name = "rateLimit")
    public boolean rateLimit() {
        String appName = System.getProperty("rateLimit");
        if (appName == null) appName = System.getenv("rateLimit");
        return (appName != null) ? Boolean.valueOf(appName) : false;
    }

    @Bean(name = "RunningInDocker")
    public boolean runningInDocker() {
        return Files.exists(Paths.get("/.dockerenv"));
    }

    @Bean(name = "bookFormatsInstalled")
    public boolean bookFormatsInstalled() {
        return applicationProperties.getSystem().getCustomApplications().isInstallBookFormats();
    }

    @Bean(name = "htmlFormatsInstalled")
    public boolean htmlFormatsInstalled() {
        return applicationProperties
                .getSystem()
                .getCustomApplications()
                .isInstallAdvancedHtmlToPDF();
    }
}
