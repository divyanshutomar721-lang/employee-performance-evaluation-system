package com.epes.filter;

import com.epes.config.AppConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * Sets UTF-8 encoding on every request and response.
 */
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(AppConstants.ENCODING);
        response.setCharacterEncoding(AppConstants.ENCODING);
        chain.doFilter(request, response);
    }
}
