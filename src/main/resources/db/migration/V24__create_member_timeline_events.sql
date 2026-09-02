CREATE TABLE member_timeline_events
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    member_id      BIGINT       NOT NULL,
    event_type     VARCHAR(40)  NOT NULL,
    title          VARCHAR(200) NOT NULL,
    description    VARCHAR(1000) NULL,
    reference_id   BIGINT NULL,
    reference_type VARCHAR(60) NULL,
    status         VARCHAR(60) NULL,
    occurred_at    DATETIME     NOT NULL,
    created_at     DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_timeline_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX          idx_member_timeline_member_occurred(member_id,occurred_at),
    INDEX          idx_member_timeline_reference(reference_type,reference_id)
);
