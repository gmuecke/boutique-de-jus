<%@ taglib prefix="s" uri="/struts-tags" %>
<h1>UserProfile</h1>

<s:form action="UserProfile_save">
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
