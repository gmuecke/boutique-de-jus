<%@taglib uri="/struts-tags" prefix="s" %>

<h1>Your shopping cart</h1>

<table>
    <s:iterator value="cart">
        <tr>
            <td><s:property value="key"/>:<s:property value="value"/></td>
            <td><s:property value="key.name"/></td>
            <td>
                <img src="<s:url action="productImage" >
                            <s:param name="id" value="key.id"/>
                          </s:url>"/>
            </td>
            <td><s:property value="key.decsription"/></td>
            <td><s:property value="key.price"/></td>
            <td><s:property value="value"/></td>
        </tr>
    </s:iterator>
</table>
<p>Total: <s:property value="total"/> $</p>
