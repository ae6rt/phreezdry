<%@ page import="com.phreezdry.server.Config" %>
<%@ page import="com.phreezdry.util.Constants" %>
<%
    Config cfg = (Config) application.getAttribute(Constants.CONFIG);
    String redirectURL = cfg.getRedirectUrl();
    response.sendRedirect(redirectURL);
%>
