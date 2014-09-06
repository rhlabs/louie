<%-- 
    Document   : index
    Created on : Jan 17, 2011, 5:04:05 PM
    Author     : cjohnson
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Hsm Services</title>
        <link rel="shortcut icon" href="favicon.ico" />
    </head>
    <body>
        <h1>Hsm Services Administration</h1>
        <a href="info">Service Info</a> <br>
        <a href="cache">Cache Management</a> <br>
        <a id="glassfish" href="#" onclick="generateUrl('4848');return false;">GlassFish Admin</a> <br>
        <a id="activemq" href="#" onclick="generateUrl('8161/admin');return false;">Active MQ Admin</a> <br>
        <a id="sourceCode" href="#" onclick="generateUrl('8080/src/louie/');return false;">Louie Source Code</a> <br>
        
        <script type='text/javascript'>
            generateUrl = function(suffix) {
                var hostname = window.location.hostname;
                self.location = "http://" + hostname + ":" + suffix;
            }
        </script>
        
        <br>
        <h4>Port Assignments</h4>
        8080 &nbsp;&nbsp;: HTTP <br>
        8787 &nbsp;&nbsp;: Authorization <br>
        61616 : TCP/openwire (ActiveMQ)<br>
        61617 : Stomp (ActiveMQ)<br>
    </body>
</html>
