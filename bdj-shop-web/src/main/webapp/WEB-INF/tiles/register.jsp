<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:form action="Register">
    <s:textfield key="name"/>
    <s:textfield key="firstname"/>
    <s:textfield key="email"/>
    <s:textfield key="street"/>
    <s:textfield key="city"/>
    <s:textfield key="zip"/>
    <s:textfield key="country"/>
    <s:textfield key="username"/>
    <s:password key="password"/>
    <s:submit/>
</s:form>

<% if (request.getAttribute("result") != null) {%>
<span><%= request.getAttribute("result")%></span>
<% } %>
