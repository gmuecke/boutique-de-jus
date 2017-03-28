package io.bdj.webshop.interceptor;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import io.bdj.webshop.auth.AuthSupport;
import io.bdj.webshop.auth.DefaultSupport;
import io.bdj.webshop.auth.JettySupport;
import org.apache.struts2.ServletActionContext;

/**
 * Interceptor that makes subsequent calls as privileged action using the current user's subject.
 */
public class RunAsInterceptor extends AbstractInterceptor {

    //TODO make support parametrizable
    private final AuthSupport defaultSupport = new DefaultSupport();
    private final AuthSupport jettySupport = new JettySupport();

    private static Function<Subject,String> runPrivileged(Callable<String> invocation){
        return subject -> {
            try {
                return Subject.doAs(subject, (PrivilegedExceptionAction<String>) invocation::call);
            } catch (PrivilegedActionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public String intercept(final ActionInvocation invocation) throws Exception {
        HttpSession session = ServletActionContext.getRequest().getSession();
        return Stream.of(defaultSupport, jettySupport)
                     .map(support -> support.apply(session))
                     .findFirst()
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .map(runPrivileged(invocation::invoke))
                     .orElse(Action.LOGIN);
    }

}
