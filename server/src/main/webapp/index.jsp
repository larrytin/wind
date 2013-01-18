<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.goodow.wind.server.servlet.InitHandler" %>

<!doctype html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <script type='text/javascript'>var __vars = <%= InitHandler.initVars().toString() %></script>
  <script type="text/javascript" src="/_ah/channel/jsapi"></script>
  <script type='text/javascript' src="view/view.nocache.js"></script>
</head>

<body>
  <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
  <noscript>
    <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
      Your web browser must have JavaScript enabled
      in order for this application to display correctly.
    </div>
  </noscript>
</body>
</html>
