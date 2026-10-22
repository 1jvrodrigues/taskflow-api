CREATE TABLE tasks (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    title       VARCHAR(120)  NOT NULL,
    description VARCHAR(1000) NULL,
    status      VARCHAR(20)   NOT NULL,
    priority    VARCHAR(20)   NOT NULL,
    due_date    DATE          NULL,
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_tasks_status (status),
    INDEX idx_tasks_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
