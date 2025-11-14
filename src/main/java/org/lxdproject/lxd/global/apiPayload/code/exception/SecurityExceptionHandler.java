package org.lxdproject.lxd.global.apiPayload.code.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lxdproject.lxd.global.apiPayload.ApiResponse;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.GeneralException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityExceptionHandler extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (GeneralException e) {
            setErrorResponse(response, e);
        }
    }

    public void setErrorResponse(HttpServletResponse response, GeneralException e) throws IOException {

        try{
            // HTTP 상태 코드 설정
            response.setStatus(e.getErrorReasonHttpStatus().getHttpStatus().value()); // 또는 e.getStatusCode()
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Object> apiResponse = ApiResponse.onFailure(e.getCode(), null);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);

            // 응답 출력
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        }catch (IOException ioException){
            throw new RuntimeException(ioException);
        }
    }
}
