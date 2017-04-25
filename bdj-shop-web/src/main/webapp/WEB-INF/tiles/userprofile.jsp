<%@ taglib prefix="s" uri="/struts-tags" %>
<h1>UserProfile</h1>

<% if (request.getAttribute("message") != null) {%>
<span class="error"><%= request.getAttribute("message")%></span>
<% } %>

<s:form action="UserProfile_execute">
    <s:label key="customer.username"/>
    <s:textfield key="customer.lastname"/>
    <s:textfield key="customer.firstname"/>
    <s:textfield key="customer.email"/>
    <s:textfield key="customer.street"/>
    <s:textfield key="customer.city"/>
    <s:textfield key="customer.zip"/>
    <s:textfield key="customer.country"/>
    <s:submit key="save" />
</s:form>
