CREATE TABLE tb_user (
                         id BIGSERIAL PRIMARY KEY,                          -- 用户主键
                         user_account VARCHAR(256) NOT NULL UNIQUE,         -- 登录账号
                         user_password VARCHAR(512) NOT NULL,               -- 密码
                         user_name VARCHAR(256),                            -- 昵称
                         user_avatar VARCHAR(1024),                         -- 头像
                         user_role VARCHAR(256) DEFAULT 'user' NOT NULL,    -- 角色: user/admin/vip
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         is_delete SMALLINT DEFAULT 0 NOT NULL              -- 逻辑删除
);

COMMENT ON TABLE tb_user IS '用户表';

CREATE TABLE tb_chat_session (
                                 id VARCHAR(128) PRIMARY KEY,                       -- 会话ID (对应 Spring AI 的 conversation_id)
                                 user_id BIGINT NOT NULL,                           -- 所属用户ID
                                 title VARCHAR(512) DEFAULT '新建对话' NOT NULL,     -- 会话标题
                                 agent_type VARCHAR(64) DEFAULT 'STANDARD' NOT NULL, -- 智能体类型: STANDARD / REASONING
                                 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                 is_delete SMALLINT DEFAULT 0 NOT NULL
);

COMMENT ON TABLE tb_chat_session IS '会话映射表';
CREATE INDEX idx_session_user_id ON tb_chat_session(user_id); -- 给用户ID加索引，方便快速查出某个人的对话历史