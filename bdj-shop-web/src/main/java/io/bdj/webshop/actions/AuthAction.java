/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bdj.webshop.actions;

import static java.util.function.Function.identity;
import static org.slf4j.LoggerFactory.getLogger;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.stream.Stream;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import io.bdj.webshop.auth.AuthSupport;
import io.bdj.webshop.auth.JettySupport;
import io.bdj.webshop.auth.LoginModuleAuthSupport;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;

/**
 * This login action performs a login using the configured LoginModules to obtain a user Subject
 * in the current session.
 */
public class AuthAction extends ActionSupport {

    private static final Logger LOG = getLogger(AuthAction.class);

    private String username;
    private String password;

    private AuthSupport jettySupport = new JettySupport();

    /**
     * Performs a login using JAAS / Container authentication. Further the method populates the session
     * attribute SUBJECT with a JAAS subject for the user. The subject is either populated from the container
     * such as jetty or an additional JAAS login is performed to obtain the subject.
     * @return
     *  SUCCESS if the login was successful, otherwise ERROR is returned.
     * @throws Exception
     */
    public String login() throws Exception {

        if (isInvalid(getUsername())) {
            return INPUT;
        }
        if (isInvalid(getPassword())) {
            return INPUT;
        }

        final HttpServletRequest request = ServletActionContext.getRequest();
        final HttpSession session = request.getSession();

        //perform container login
        request.login(username, password);

        //try to obtain the Subject from the container or perform an _additional_ login
        //in order to get the subject.
        final Optional<Subject> subject = Stream.of(jettySupport,
                                                    new LoginModuleAuthSupport(getUsername(), getPassword()))
                                                .map(f -> f.apply(session))
                                                .findFirst()
                                                .flatMap(identity());

        //register the subject in the session so we can obtain it without vendor specific
        //access logic (such as Jetty's)
        //see RunAsInterceptor where we need this
        subject.ifPresent(subj -> session.setAttribute("SUBJECT", subj));
        return subject.map(s -> Action.SUCCESS).orElse(Action.ERROR);
    }

    /**
     * Logs out the user and invalidates the session
     * @return
     *  the method always returns SUCCESS or throws an error if logout fails.
     * @throws ServletException
     */
    public String logout() throws ServletException {
        final HttpServletRequest request = ServletActionContext.getRequest();
        final HttpSession session = request.getSession();

        //perform container login
        request.logout();
        session.invalidate();
        return SUCCESS;
    }

    private boolean isInvalid(String value) {

        return (value == null || value.length() == 0);
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

}
