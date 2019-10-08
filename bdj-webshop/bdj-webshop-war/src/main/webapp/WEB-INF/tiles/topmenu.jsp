<%@taglib uri="/struts-tags" prefix="s" %>
<%
    String username = request.getRemoteUser();
%>
<ul>
    <%-- Left part starts here (from left right) --%>
    <li>
        <a href="<s:url action="index" namespace="/"/>">Home</a>
    </li>
    <% if (username != null) { %>
    <li class="info">
        <span id="loginInfo"> Logged in as <%= username %></span>
    </li>
    <%}%>
    <%-- Right part starts here (from right to left) --%>
    <li class="floatRight">
        <a href="<s:url action="help" namespace="/"/>">Help</a><br>
    </li>

    <% if (username != null) { %>
    <li class="floatRight">
        <a href="<s:url action="UserProfile_input" namespace="/secure"/>">UserProfile</a><br>
    </li>
    <li class="floatRight">
        <a href="<s:url action="Logout" namespace="/"/>">Logout</a><br>
    </li>
    <%} else { %>
    <li class="floatRight">
        <a href="<s:url action="register_input" namespace="/"/>">Register</a><br>
    </li>
    <li class="floatRight">
        <a href="<s:url action="Login" namespace="/"/>">Login</a><br>
    </li>
    <%}%>

    <li class="floatRight">
        <a href="<s:url action="cart_" namespace="/"/>">Cart</a><br>
    </li>
</ul>
