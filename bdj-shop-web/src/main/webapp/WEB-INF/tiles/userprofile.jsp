<%@ page import="javax.security.auth.Subject" %>
<%@ page import="java.security.AccessControlContext" %>
<%@ page import="java.security.AccessController" %>
<h1>UserProfile</h1>

<%
    String username = request.getRemoteUser();
%>
<span>Hello <%= username %>. This is a secure resource</span>

<%
    AccessControlContext acc = AccessController.getContext();
    System.out.println(Subject.getSubject(acc));

%>
