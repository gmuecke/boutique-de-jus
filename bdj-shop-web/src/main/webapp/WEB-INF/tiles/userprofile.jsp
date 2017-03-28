<%@ taglib prefix="s" uri="/struts-tags" %>
<h1>UserProfile</h1>

<ul>
    <s:iterator value="names">
        <ul><s:property/></ul>
    </s:iterator>
</ul>

