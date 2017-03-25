package io.bdj.webshop.auth;

import static io.bdj.webshop.util.Reflections.invokeMethod;

import javax.security.auth.Subject;
import java.util.Optional;

/**
 * Support to obtain the current subject from the Session for Jetty 9.4
 */
public class JettySupport implements AuthSupport {

    public static final String USER_IDENTIY_SESSION_KEY = "org.eclipse.jetty.security.UserIdentity";

    @Override
    public String getSubjectKey() {

        return USER_IDENTIY_SESSION_KEY;
    }

    @Override
    public Optional<Subject> map(final Optional<Object> o) {

        /*
         * We can not use the the classes directly because they are loaded by a different classloader
         * (jetty's CL, not the web app's CL). An the subject is hidden behind two method calls.
         */
        return o.map(userIdentity -> (Subject) invokeMethod("getUserIdentity").andThen(invokeMethod("getSubject"))
                                                                              .apply(userIdentity));
    }
}
