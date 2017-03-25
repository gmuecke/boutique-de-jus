package io.bdj.webshop.auth;

import static org.slf4j.LoggerFactory.getLogger;

import javax.security.auth.Subject;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

/**
 *
 */
public class LoginModuleAuthSupport implements AuthSupport {

    private static final Logger LOG = getLogger(LoginModuleAuthSupport.class);

    private String username;
    private String password;

    public LoginModuleAuthSupport(final String username, final String password) {

        this.username = username;
        this.password = password;
    }

    @Override
    public String getSubjectKey() {
        return "";
    }

    @Override
    public Optional<Subject> map(final Optional<Object> wrapper) {
        try {
            LOG.info("logging in user as {}", username);
            LoginContext lc = new LoginContext("PropertyFile", callbacks -> {
                LOG.info("Callback Handler invoked ");
                Stream.of(callbacks).forEach(cb -> {
                    if (cb instanceof NameCallback) {
                        ((NameCallback) cb).setName(username);
                    } else if (cb instanceof PasswordCallback) {
                        ((PasswordCallback) cb).setPassword(password.toCharArray());
                    }
                });
            });
            lc.login();
            return Optional.of(lc.getSubject());
        } catch (LoginException e) {
            LOG.error("Authentication failed", e);
            return Optional.empty();
        }
    }
}
