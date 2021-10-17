package htwb.ai.songsservice.filter;

import htwb.ai.songsservice.common.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@AllArgsConstructor
public class SecurityServletFilter extends HttpFilter {

    @Autowired
    private final RestTemplate template;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String token = extractAuthTokenFromRequest(request);

        if (token == null || !isAuthenticated(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractAuthTokenFromRequest(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private boolean isAuthenticated(String token) {
        User user = template.getForObject("http://AUTH-SERVICE/auth?auth_token=" + token, User.class);
        return user != null;
    }
}