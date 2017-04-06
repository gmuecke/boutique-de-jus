<%@ taglib prefix="s" uri="/struts-tags" %>
<h1>UserProfile</h1>

<s:form action="UserProfile_save">
    <s:label key="username"/>
    <s:textfield key="lastname"/>
    <s:textfield key="firstname"/>
    <s:textfield key="email"/>
    <s:textfield key="street"/>
    <s:textfield key="city"/>
    <s:textfield key="zip"/>
    <s:textfield key="country"/>
    <s:submit key="save" />
</s:form>
