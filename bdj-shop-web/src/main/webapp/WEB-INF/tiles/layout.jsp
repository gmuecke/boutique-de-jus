<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><s:text name="Welcome.page.title"/></title>
        <link rel="stylesheet" href="/style/bdj.css">
        <link rel="stylesheet" href="/style/compat.css">
    </head>
    <body>
        <div class="flex-container">
            <nav class="top">
                <tiles:insertAttribute name="topmenu"/>
            </nav>
            <nav class="side">
                <tiles:insertAttribute name="menu"/>
            </nav>
            <header>
                <tiles:insertAttribute name="header"/>
            </header>
            <main id="main" class="main">
                <tiles:insertAttribute name="body"/>
            </main>
            <footer>
                <tiles:insertAttribute name="footer"/>
            </footer>
        </div>
    </body>
</html>
