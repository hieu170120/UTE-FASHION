package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Debug Filter để log tất cả requests
 */
@Component
public class DebugFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // Chỉ log các request quan trọng
        if (requestURI.contains("verify-email") || requestURI.contains("register") || requestURI.contains("api/auth")) {
            System.out.println("=== DEBUG REQUEST ===");
            System.out.println("Method: " + method);
            System.out.println("URI: " + requestURI);
            System.out.println("Query String: " + httpRequest.getQueryString());
            
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                System.out.println("Session ID: " + session.getId());
                System.out.println("Session attributes:");
                java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attrName = attributeNames.nextElement();
                    Object attrValue = session.getAttribute(attrName);
                    System.out.println("  " + attrName + " = " + attrValue);
                }
            } else {
                System.out.println("No session found");
            }
            System.out.println("=====================");
        }
        
        chain.doFilter(request, response);
        
        // Log response status
        if (requestURI.contains("verify-email") || requestURI.contains("register") || requestURI.contains("api/auth")) {
            System.out.println("Response Status: " + httpResponse.getStatus());
            System.out.println("=====================");
        }
    }
}

