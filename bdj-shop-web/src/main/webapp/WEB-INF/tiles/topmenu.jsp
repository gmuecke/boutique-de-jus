<%@taglib uri="/struts-tags" prefix="s" %>

<ul>
    <li>
        <a href="<s:url action="index" namespace="/"/>">Home</a>
    </li>
    <li><span>
        <%
            String username = request.getRemoteUser();
            if (username != null) {
        %>Hello <%= username %>. This is a secure resource<%}%>
    </span>
    </li>
    <li style="float:right">
        <a href="<s:url action="Login" namespace="/"/>">Login</a><br>
    </li>
    <li style="float:right">
        <a href="<s:url action="help" namespace="/"/>">Help</a><br>
    </li>
</ul>
