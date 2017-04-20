<%@taglib uri="/struts-tags" prefix="s" %>
<%
    String username = request.getRemoteUser();
%>
<ul>
    <li>
        <a href="<s:url action="index" namespace="/"/>">Home</a>
    </li>
    <% if (username != null) { %>
    <li>
        <span> Hello <%= username %>. This is a secure resource</span>
    </li>
    <%}%>
    <li style="float:right">
        <a href="<s:url action="cart_" namespace="/"/>">Cart</a><br>
    </li>
    <% if (username != null) { %>
    <li style="float:right">
        <a href="<s:url action="Logout" namespace="/"/>">Logout</a><br>
    </li>
    <%} else { %>
    <li style="float:right">
        <a href="<s:url action="Register" namespace="/"/>">Register</a><br>
    </li>
    <li style="float:right">
        <a href="<s:url action="Login" namespace="/"/>">Login</a><br>
    </li>
    <%}%>
    <li style="float:right">
        <a href="<s:url action="help" namespace="/"/>">Help</a><br>
    </li>
</ul>
