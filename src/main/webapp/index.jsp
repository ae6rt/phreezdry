<%@ page import="com.phreezdry.server.Config" %>
<%@ page import="com.phreezdry.util.Constants" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%
    ApplicationContext appContext = (ApplicationContext) application.getAttribute(Constants.CONTEXT);
    Config cfg = (Config) appContext.getBean(Constants.CONFIG);
    String redirectUrl = cfg.getRedirectUrl();
    response.sendRedirect(redirectUrl);
%>
