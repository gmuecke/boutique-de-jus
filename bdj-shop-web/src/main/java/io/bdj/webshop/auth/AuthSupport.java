package io.bdj.webshop.auth;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.function.Function;

/**
 * Support for accessing a Jaas Subject directly from the session. As this access is vendor specific,
 * the interface my be implemented for specific web servers.
 */
public interface AuthSupport extends Function<HttpSession, Optional<Subject>>{

    String getSubjectKey();

    Optional<Subject> map(Optional<Object> wrapper);

    @Override
    default Optional<Subject> apply(HttpSession session){
        return map(Optional.ofNullable(session.getAttribute(getSubjectKey())));
    }

}
