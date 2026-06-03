CREATE TABLE IF NOT EXISTS user_equipped_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_pk_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_purchased_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_pk_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
