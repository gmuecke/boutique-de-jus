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
        <script type="text/javascript" src="/scripts/jquery-3.4.1.min.js"></script>
        <script>
            function fixNavToTop(){
                if ((window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0) > 149) {
                    $("#sidemenu").addClass("fixed-sidenav");
                } else {
                    $("#sidemenu").removeClass("fixed-sidenav");
                }
            }
        </script>
    </head>
    <body onscroll="fixNavToTop()">
        <div class="flex-container">
            <nav class="top ternary-inv">
                <tiles:insertAttribute name="topmenu"/>
            </nav>
            <nav class="side secondary" id="sidemenu">
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
