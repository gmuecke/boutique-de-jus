<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<h1>Login</h1>
<s:form action="Login">
    <s:textfield key="username"/>
    <s:password key="password" />
    <s:submit value="Login"/>
</s:form>
