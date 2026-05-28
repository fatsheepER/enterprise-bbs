ALTER TABLE posts
    DROP CONSTRAINT IF EXISTS fk_posts_board;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_board
    FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE;

ALTER TABLE replies
    DROP CONSTRAINT IF EXISTS fk_replies_post;

ALTER TABLE replies
    ADD CONSTRAINT fk_replies_post
    FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;

ALTER TABLE replies
    DROP CONSTRAINT IF EXISTS fk_replies_parent_reply;
