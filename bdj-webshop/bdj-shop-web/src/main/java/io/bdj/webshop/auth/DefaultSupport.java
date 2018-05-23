package io.bdj.webshop.auth;

import javax.security.auth.Subject;
import java.util.Optional;

/**
 *
 */
public class DefaultSupport implements AuthSupport {

    public static final String SUBJECT_ATTRIBUTE = "SUBJECT";

    @Override
    public String getSubjectKey() {

        return SUBJECT_ATTRIBUTE;
    }

    @Override
    public Optional<Subject> map(final Optional<Object> subject) {

        return subject.filter(o -> o instanceof Subject).map(o -> (Subject) o);
    }
}
