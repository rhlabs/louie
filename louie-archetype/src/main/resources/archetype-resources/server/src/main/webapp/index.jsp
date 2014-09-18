#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${rootArtifactId}</title>
        <link rel="shortcut icon" href="favicon.ico" />
    </head>
    <body>
        <h1>${rootArtifactId} Administration</h1>
        <a href="info">Service Info</a> <br>
        <a href="cache">Cache Management</a> <br>
        
        <script type='text/javascript'>
            generateUrl = function(suffix) {
                var hostname = window.location.hostname;
                self.location = "http://" + hostname + ":" + suffix;
            }
        </script>
        
    </body>
</html>
