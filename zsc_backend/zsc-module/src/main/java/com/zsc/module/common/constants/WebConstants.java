package com.zsc.module.common.constants;

public class WebConstants {
    // 角色ID（注意与数据库保持一致，如后续变更请同步）
    public static final long ROLE_SUPER_ADMIN = 1;
    public static final long ROLE_ADMIN = 2;
    public static final long ROLE_RESIDENT = 3;     // 居民

    // 状态
    public static final int STATUS_ACTIVE = 1;     // 激活
    public static final int STATUS_INACTIVE = 0;   // 停用

    // 默认密码等
    public static final String DEFAULT_PASSWORD = "123456";

    // 分页参数
     public static final int DEFAULT_PAGE_SIZE = 10;

    // 系统信息
    public static final String SYSTEM_NAME = "社区服务管理系统";
    public static final String COPYRIGHT = "Copyright © 2025 社区服务平台";


}

