DROP TABLE IF EXISTS replies CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS boards CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL DEFAULT '',
    avatar VARCHAR(255) NOT NULL DEFAULT '',
    email VARCHAR(100),
    bio VARCHAR(500) NOT NULL DEFAULT '',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_users_username_not_blank CHECK (btrim(username) <> ''),
    CONSTRAINT ck_users_password_not_blank CHECK (btrim(password) <> ''),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE boards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL DEFAULT '',
    color_hex VARCHAR(20) NOT NULL DEFAULT '#007aff',
    sort_order INTEGER NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_boards_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_boards_status CHECK (status IN (0, 1))
);

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    view_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_board FOREIGN KEY (board_id) REFERENCES boards (id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_posts_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT ck_posts_content_not_blank CHECK (btrim(content) <> ''),
    CONSTRAINT ck_posts_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_posts_view_count CHECK (view_count >= 0)
);

CREATE TABLE replies (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_reply_id BIGINT,
    content TEXT NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_replies_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_replies_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_replies_parent_reply FOREIGN KEY (parent_reply_id) REFERENCES replies (id) ON DELETE SET NULL,
    CONSTRAINT ck_replies_content_not_blank CHECK (btrim(content) <> ''),
    CONSTRAINT ck_replies_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_replies_not_self_parent CHECK (parent_reply_id IS NULL OR parent_reply_id <> id)
);

CREATE INDEX idx_boards_status_sort ON boards (status, sort_order, id);
CREATE INDEX idx_posts_board_status_updated ON posts (board_id, status, updated_at DESC);
CREATE INDEX idx_posts_user ON posts (user_id);
CREATE INDEX idx_posts_status_updated ON posts (status, updated_at DESC);
CREATE INDEX idx_replies_post_status_created ON replies (post_id, status, created_at ASC);
CREATE INDEX idx_replies_user ON replies (user_id);
CREATE INDEX idx_replies_parent_reply ON replies (parent_reply_id);
