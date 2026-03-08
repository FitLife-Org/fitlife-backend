-- V5: Bổ sung bảng lịch sử điểm danh (Check-in)
CREATE TABLE check_in_history (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  check_in_time DATETIME NOT NULL,
                                  status VARCHAR(20) NOT NULL,
                                  CONSTRAINT fk_checkin_member FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;