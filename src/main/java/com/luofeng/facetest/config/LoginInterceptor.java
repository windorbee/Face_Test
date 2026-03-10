package com.luofeng.facetest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luofeng.facetest.pojo.Result;
import com.luofeng.facetest.utils.JwtUtils;
import com.luofeng.facetest.utils.UserThreadLocal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求URL
        String url = request.getRequestURL().toString();
        log.info("请求URL: {}", url);
        
        // 放行登录和注册相关接口
        if (url.contains("/login") || url.contains("/register")) {
            log.info("放行登录/注册接口");
            return true;
        }
        
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        // 判断token是否为空或无效
        if (!StringUtils.hasLength(token)) {
            log.info("请求头中token为空");
            responseUnauthorized(response, "未登录，请先登录");
            return false;
        }
        
        // 验证token
        try {
            Claims claims = JwtUtils.parseJWT(token);
            
            // 检查token是否即将过期（提前10分钟提示）
            Date expiration = claims.getExpiration();
            long timeToExpire = expiration.getTime() - System.currentTimeMillis();
            if (timeToExpire < 600000) { // 10分钟
                log.info("token即将过期，剩余时间: {} 毫秒", timeToExpire);
            }
            
            // 将用户信息存入请求域中
            Integer userId = (Integer) claims.get("id");
            String username = (String) claims.get("username");
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            
            // 将用户ID存入ThreadLocal中，以便在当前线程中使用
            UserThreadLocal.setUserId(userId);
            
            log.info("用户已登录，用户ID: {}, 用户名: {}", userId, username);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("token已过期: {}", e.getMessage());
            responseUnauthorized(response, "登录已过期，请重新登录");
            return false;
        } catch (SignatureException e) {
            log.info("token签名无效: {}", e.getMessage());
            responseUnauthorized(response, "登录凭证无效，请重新登录");
            return false;
        } catch (MalformedJwtException e) {
            log.info("token格式错误: {}", e.getMessage());
            responseUnauthorized(response, "登录凭证格式错误，请重新登录");
            return false;
        } catch (Exception e) {
            log.info("token解析失败: {}", e.getMessage());
            responseUnauthorized(response, "登录凭证验证失败，请重新登录");
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal中的用户ID，防止内存泄漏
        UserThreadLocal.clear();
    }
    
    /**
     * 响应未授权错误信息
     */
    private void responseUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(message)));
    }
}