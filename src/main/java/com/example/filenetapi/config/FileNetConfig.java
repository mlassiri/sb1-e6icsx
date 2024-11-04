package com.example.filenetapi.config;

import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.Subject;
import java.util.Properties;

@Configuration
public class FileNetConfig {

    @Value("${filenet.url}")
    private String url;

    @Value("${filenet.username}")
    private String username;

    @Value("${filenet.password}")
    private String password;

    @Value("${filenet.objectstore}")
    private String objectStoreName;

    @Bean
    public Connection getConnection() {
        Properties props = new Properties();
        props.setProperty(Connection.TRANSPORT_HTTP_URL, url);
        return Factory.Connection.getConnection(url);
    }

    @Bean
    public ObjectStore getObjectStore(Connection connection) {
        Subject subject = UserContext.createSubject(connection, username, password, null);
        UserContext.get().pushSubject(subject);

        Domain domain = Factory.Domain.fetchInstance(connection, null, null);
        return Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
    }
}