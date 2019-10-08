<%@taglib uri="/struts-tags" prefix="s" %>

<h1>Your shopping cart</h1>
<% if (request.getAttribute("message") != null) {%>
<span class="error"><%= request.getAttribute("message")%></span>
<% } %>
<table>
    <thead>
        <tr>
            <th>Image</th>
            <th>Name</th>
            <th>Price</th>
            <th>Quantity</th>
        </tr>

    </thead>
    <s:iterator value="cart">
        <tr>
            <td>
                <img src="<s:url action="productImage" >
                            <s:param name="id" value="key.id"/>
                          </s:url>"/>
            </td>
            <td><s:property value="key.name"/></td>
            <td><s:property value="key.price"/></td>
            <td><s:property value="value"/></td>
        </tr>
    </s:iterator>
    <tfoot>
        <tr>
            <td colspan="3">Total:</td>
            <td><s:property value="total"/> $</td>
        </tr>
    </tfoot>
</table>

<a href="<s:url action="order_input" namespace="/secure"/>" >Submit Order</a><br>
