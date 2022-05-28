package com.example.rupbank;

import com.example.rupbank.controller.HomepageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    Logger logger = LoggerFactory.getLogger(HomepageController.class);

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response)
            throws AuthenticationException {

        // ...

        UsernamePasswordAuthenticationToken authRequest
                = getAuthRequest(request);
        setDetails(request, authRequest);

        return this.getAuthenticationManager()
                .authenticate(authRequest);
    }

    private UsernamePasswordAuthenticationToken getAuthRequest(
            HttpServletRequest request) {

        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String bankId = obtainBankId(request);

        // ...
        logger.warn(String.format("Bank: %s", bankId));
        request.getSession().setAttribute("bank_id", bankId);
        request.getSession().setAttribute("customer_email", username);

        String usernameBankId = String.format("%s%s%s", username.trim(),
                String.valueOf(Character.LINE_SEPARATOR), bankId);
        return new UsernamePasswordAuthenticationToken(
                usernameBankId, password);
    }

    // other methods
    @Nullable
    protected String obtainBankId(HttpServletRequest request) {
        return request.getParameter("bank_id");
    }
}