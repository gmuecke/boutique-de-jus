<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:url forceAddSchemeHostAndPort="true" includeParams="all" var="url"/>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><s:text name="Welcome.page.title"/></title>
        <link rel="stylesheet" href="/style/bdj.css">
        <link rel="stylesheet" href="/style/compat.css">
        <link rel="stylesheet" href="/style/colors.css">
    </head>
    <body>
        <div class="flex-container">
            <nav class="top ternary-inv">
                <tiles:insertAttribute name="topmenu"/>
            </nav>
            <nav class="side ternary">
                <tiles:insertAttribute name="menu"/>
            </nav>
            <header class="secondary">
                <tiles:insertAttribute name="header"/>
            </header>
            <main id="main" class="main primary">
                <tiles:insertAttribute name="body" />
            </main>
            <footer class="secondary">
                <tiles:insertAttribute name="footer"/>
            </footer>
        </div>
    </body>
</html>
