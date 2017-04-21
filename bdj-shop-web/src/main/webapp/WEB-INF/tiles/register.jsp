<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:form action="Register">
    <s:textfield key="customer.lastname"/>
    <s:textfield key="customer.firstname"/>
    <s:textfield key="customer.email"/>
    <s:textfield key="customer.street"/>
    <s:textfield key="customer.city"/>
    <s:textfield key="customer.zip"/>
    <s:textfield key="customer.country"/>
    <s:textfield key="customer.username"/>
    <s:password key="customer.password"/>
    <s:submit/>
</s:form>

<% if (request.getAttribute("result") != null) {%>
<span><%= request.getAttribute("result")%></span>
<% } %>
