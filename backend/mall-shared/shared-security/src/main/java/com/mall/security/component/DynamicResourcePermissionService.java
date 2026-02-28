package com.mall.security.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 动态资源权限校验 — 基于 ums_resource 表的 URL Pattern 匹配
 * <p>
 * 仅 Admin 端使用。在 SecurityConfig 中通过
 * {@code .access((auth, ctx) -> new AuthorizationDecision(dynamicService.hasAccess(auth.get(), ctx.getRequest())))}
 * 接入。
 */
@Service
public class DynamicResourcePermissionService {
    private final JdbcTemplate jdbcTemplate;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public DynamicResourcePermissionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasAccess(Authentication authentication, HttpServletRequest request) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        String username = authentication.getName();
        if (!StringUtils.hasText(username)) {
            return false;
        }

        // 超级管理员直接放行
        if ("admin".equalsIgnoreCase(username)) {
            return true;
        }

        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return false;
        }

        // 白名单路径
        if (isWhitelistPath(path)) {
            return true;
        }

        // 加载所有资源 URL pattern
        List<ResourcePatternRow> resources = loadResources();
        if (resources.isEmpty()) {
            return true;
        }

        // 匹配当前路径命中的资源 ID
        List<Long> matchedResourceIds = resources.stream()
                .filter(item -> antPathMatcher.match(item.urlPattern(), path))
                .map(ResourcePatternRow::resourceId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (matchedResourceIds.isEmpty()) {
            return true;
        }

        // 查询当前用户的 admin ID
        Long adminId = jdbcTemplate.query(
                "SELECT id FROM ums_admin WHERE username = ? LIMIT 1",
                rs -> rs.next() ? rs.getLong("id") : null,
                username
        );
        if (adminId == null) {
            return false;
        }

        // 查询用户关联的角色
        List<Long> roleIds = jdbcTemplate.query(
                "SELECT role_id FROM ums_admin_role_relation WHERE admin_id = ?",
                (rs, rowNum) -> (Long) rs.getObject("role_id"),
                adminId
        ).stream().filter(id -> id != null && id > 0).distinct().toList();
        if (roleIds.isEmpty()) {
            return false;
        }

        // 检查角色是否拥有匹配的资源权限
        String rolePlaceholders = String.join(",", Collections.nCopies(roleIds.size(), "?"));
        String resourcePlaceholders = String.join(",", Collections.nCopies(matchedResourceIds.size(), "?"));
        String sql = "SELECT COUNT(1) FROM ums_role_resource_relation WHERE role_id IN ("
                + rolePlaceholders + ") AND resource_id IN (" + resourcePlaceholders + ")";

        List<Object> params = new ArrayList<>(roleIds);
        params.addAll(matchedResourceIds);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
        return count != null && count > 0;
    }

    private List<ResourcePatternRow> loadResources() {
        return jdbcTemplate.query(
                "SELECT id, url FROM ums_resource WHERE url IS NOT NULL AND url <> ''",
                (rs, rowNum) -> new ResourcePatternRow(
                        (Long) rs.getObject("id"),
                        rs.getString("url")
                )
        );
    }

    private boolean isWhitelistPath(String path) {
        return path.startsWith("/actuator/")
                || path.startsWith("/admin/login")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources");
    }

    private record ResourcePatternRow(Long resourceId, String urlPattern) {}
}
