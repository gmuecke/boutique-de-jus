<%@taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Juicy  <tiles:getAsString name="category" ignore="true" /></h1>

<table id="products">
    <thead>
    <tr>
        <th class="product_id">ID</th>
        <th class="product_name">Name</th>
        <th class="product_image">Image</th>
        <th class="product_desc">Description</th>
        <th class="product_cat">Category</th>
        <th class="product_tags">Tags</th>
        <th class="product_price">Price</th>
        <th class="product_add">&nbsp;</th>
    </tr>
    </thead>
    <s:iterator value="products">
        <tr>
            <td class="product_id"><s:property value="id"/></td>
            <td class="product_name"><s:property value="name"/></td>
            <td class="product_image">
                <img src="<s:url action="productImage" >
                            <s:param name="id" value="%{id}"/>
                          </s:url>"/>
            </td>
            <td class="product_desc"><s:property value="description"/></td>
            <td class="product_cat"><s:property value="category"/></td>
            <td class="product_tags"><s:property value="tags"/></td>
            <td class="product_price"><s:property value="price"/></td>
            <td class="product_add">
                <s:form action="cart_add" validate="true" >
                    <s:hidden name="url" value="%{url}"/>
                    <s:textfield name="quantity" key="quantity" type="number"/>
                    <s:hidden name="id" value="%{id}"/>
                    <s:submit value="Add"/>
                </s:form>
            </td>
        </tr>
    </s:iterator>
</table>
