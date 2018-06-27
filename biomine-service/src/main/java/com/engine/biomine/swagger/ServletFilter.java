package com.engine.biomine.swagger;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class ServletFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// Add CORS HTTP headers to allow cross origin sharing, for instance,
		// to allow Swagger UI to consume service)
		HttpServletResponse res = (HttpServletResponse) response;
		res.addHeader("Access-Control-Allow-Origin", "*");
		res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
		res.addHeader("Access-Control-Allow-Headers", "Content-Type");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}
}
