package com.mainthreadlab.weinv.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@Getter
@ConfigurationProperties("security")
public class SecurityProperties {

    private JwtProperties jwt;

    public void setJwt(JwtProperties jwt) {
        this.jwt = jwt;
    }

    @Getter
    public static class JwtProperties {

        private Resource keyStore;
        private String keyStorePassword;
        private String keyPairAlias;
        private String jwkKid;
        private String keyPairPassword;

        public void setKeyStore(Resource keyStore) {
            this.keyStore = keyStore;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public void setKeyPairAlias(String keyPairAlias) {
            this.keyPairAlias = keyPairAlias;
        }

        public void setKeyPairPassword(String keyPairPassword) {
            this.keyPairPassword = keyPairPassword;
        }

        public void setJwkKid(String jwkKid) {
            this.jwkKid = jwkKid;
        }
    }
}
