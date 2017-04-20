<%@taglib uri="/struts-tags" prefix="s" %>

<h1>Juice Course Catalog</h1>

<table>
    <s:iterator value="products">
        <tr>
            <td><s:property value="id"/></td>
            <td><s:property value="name"/></td>
            <td>
                <img src="<s:url action="productImage" >
                            <s:param name="id" value="%{id}"/>
                          </s:url>"/>
            </td>
            <td><s:property value="decsription"/></td>
            <td><s:property value="category"/></td>
            <td><s:property value="tags"/></td>
            <td><s:property value="price"/></td>
            <td>
                <s:form action="cart_add">
                    <s:hidden name="url" value="%{url}"/>
                    <s:textfield name="quantity" key="quantity"/>
                    <s:hidden name="id" value="%{id}"/>
                    <s:submit/>
                </s:form>
            </td>
        </tr>
    </s:iterator>
</table>
