<%@taglib uri="/struts-tags" prefix="s" %>

<h1>Confirm your order</h1>

<p>Ship to:</p>

<ul>
    <li><s:label key="customer.lastname"/>, <s:label key="customer.firstname"/></li>
    <li>
        <s:label key="customer.street"/>
    </li>
    <li>
        <s:label key="customer.city"/>,
        <s:label key="customer.zip"/>
    </li>
    <li>
        <s:label key="customer.country"/>
    </li>
</ul>
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
                <img src="<s:url action="productImage" namespace="/">
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
<s:form action="order_execute" namespace="/secure">
    <s:submit key="order" />
</s:form>
