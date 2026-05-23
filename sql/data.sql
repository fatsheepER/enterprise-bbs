TRUNCATE TABLE replies, posts, boards, users RESTART IDENTITY CASCADE;

INSERT INTO users (
    id, username, password, nickname, avatar, email, bio, role, created_at, updated_at
) VALUES
    (1, 'admin', 'admin123', '系统管理员', '', 'admin@enterprise-bbs.local', '负责版块维护和帖子管理。', 'ADMIN', '2026-05-20 09:00:00', '2026-05-20 09:00:00'),
    (2, 'alice', '123456', 'Alice 王', '', 'alice@enterprise-bbs.local', '前端开发工程师，关注 Vue 和交互体验。', 'USER', '2026-05-20 09:10:00', '2026-05-20 09:10:00'),
    (3, 'bob', '123456', 'Bob 李', '', 'bob@enterprise-bbs.local', '后端开发工程师，关注 Spring Boot 和数据库。', 'USER', '2026-05-20 09:20:00', '2026-05-20 09:20:00'),
    (4, 'carol', '123456', 'Carol 陈', '', 'carol@enterprise-bbs.local', '测试工程师，负责接口联调和演示验收。', 'USER', '2026-05-20 09:30:00', '2026-05-20 09:30:00');

INSERT INTO boards (
    id, name, description, color_hex, sort_order, status, created_at, updated_at
) VALUES
    (1, '技术交流', '讨论前端、后端、数据库和工程化相关问题。', '#007aff', 1, 1, '2026-05-20 10:00:00', '2026-05-23 15:40:00'),
    (2, '项目公告', '发布课程设计进度、系统通知和演示安排。', '#ff9500', 2, 1, '2026-05-20 10:05:00', '2026-05-23 14:20:00'),
    (3, '需求讨论', '沉淀需求变更、页面交互和验收口径。', '#34c759', 3, 1, '2026-05-20 10:10:00', '2026-05-23 13:30:00'),
    (4, '资料归档', '归档报告材料、接口文档和演示数据说明。', '#5856d6', 4, 1, '2026-05-20 10:15:00', '2026-05-22 18:00:00'),
    (5, '停用版块示例', '用于管理端演示启用和停用状态。', '#8e8e93', 99, 0, '2026-05-20 10:20:00', '2026-05-20 10:20:00');

INSERT INTO posts (
    id, board_id, user_id, title, content, status, view_count, created_at, updated_at
) VALUES
    (
        1,
        1,
        2,
        '如何统一前后端接口返回格式？',
        '我在整理前端 Axios 封装，希望所有接口都返回统一的 code、message、data 和 timestamp。大家觉得后端 Result<T> 应该怎么设计，分页数据应该放在哪里？',
        1,
        42,
        '2026-05-21 09:30:00',
        '2026-05-23 15:40:00'
    ),
    (
        2,
        1,
        3,
        'PostgreSQL 表结构里的 status 字段怎么约定？',
        '目前 users、boards、posts、replies 四张表都需要支持演示。帖子和回复想做软删除，版块想做停用。是否统一用 status = 1 正常，status = 0 隐藏或停用？',
        1,
        28,
        '2026-05-21 10:10:00',
        '2026-05-22 16:15:00'
    ),
    (
        3,
        2,
        1,
        '课程设计第一版 demo 数据已准备',
        '管理员账号 admin / admin123，普通用户账号 alice / 123456、bob / 123456。请优先验证登录、发帖、回复和管理端版块维护流程。',
        1,
        65,
        '2026-05-21 14:00:00',
        '2026-05-23 14:20:00'
    ),
    (
        4,
        3,
        4,
        '帖子详情页是否需要复杂嵌套回复？',
        '根据基础方案，回复支持 parent_reply_id，但前端不做复杂树形嵌套，只在回复上方展示被引用回复的摘要。这样更适合快速完成 demo。',
        1,
        31,
        '2026-05-22 11:00:00',
        '2026-05-23 13:30:00'
    ),
    (
        5,
        4,
        2,
        'API 协议和数据库脚本归档位置',
        'API 协议放在 docs/API-CONTRACT.md，PostgreSQL 建表脚本放在 sql/schema.sql，演示数据放在 sql/data.sql。',
        1,
        19,
        '2026-05-22 17:20:00',
        '2026-05-22 18:00:00'
    ),
    (
        6,
        3,
        3,
        '已隐藏帖子示例',
        '这条帖子用于管理端帖子状态筛选演示，前台列表不应该展示。',
        0,
        7,
        '2026-05-22 19:00:00',
        '2026-05-22 19:30:00'
    );

INSERT INTO replies (
    id, post_id, user_id, parent_reply_id, content, status, created_at, updated_at
) VALUES
    (1, 1, 3, NULL, '建议后端统一封装 Result<T>，成功时 code 固定为 200，错误时返回业务错误码。', 1, '2026-05-21 09:45:00', '2026-05-21 09:45:00'),
    (2, 1, 4, 1, '赞同。分页也可以统一放在 data 里，包含 list、total、page、pageSize、totalPages。', 1, '2026-05-21 10:05:00', '2026-05-21 10:05:00'),
    (3, 1, 2, 2, '这样前端 mock 很方便，Axios 拦截器只需要判断 code。', 1, '2026-05-23 15:40:00', '2026-05-23 15:40:00'),
    (4, 2, 2, NULL, '可以统一。boards 的 0 表示停用，posts 和 replies 的 0 表示隐藏或软删除。', 1, '2026-05-21 10:30:00', '2026-05-21 10:30:00'),
    (5, 2, 1, 4, '管理端删除版块和帖子都先做软删除，演示更稳。', 1, '2026-05-22 16:15:00', '2026-05-22 16:15:00'),
    (6, 3, 2, NULL, '已验证普通用户登录和浏览帖子列表。', 1, '2026-05-21 14:20:00', '2026-05-21 14:20:00'),
    (7, 3, 3, NULL, '后端联调时可以直接用这些账号测试权限。', 1, '2026-05-23 14:20:00', '2026-05-23 14:20:00'),
    (8, 4, 2, NULL, '不建议做树形 UI，引用摘要已经能体现 parent_reply_id 的功能。', 1, '2026-05-22 12:00:00', '2026-05-22 12:00:00'),
    (9, 4, 4, 8, '那详情页就按时间顺序展示回复，并在引用回复上方放一个 callout。', 1, '2026-05-23 13:30:00', '2026-05-23 13:30:00'),
    (10, 6, 1, NULL, '这条回复同样用于隐藏帖子场景，前台不需要展示。', 0, '2026-05-22 19:30:00', '2026-05-22 19:30:00');

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('boards', 'id'), (SELECT MAX(id) FROM boards));
SELECT setval(pg_get_serial_sequence('posts', 'id'), (SELECT MAX(id) FROM posts));
SELECT setval(pg_get_serial_sequence('replies', 'id'), (SELECT MAX(id) FROM replies));
