package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot dữ liệu User tối thiểu cần gửi cho AI.
 *
 * Không gửi email, phone hoặc dữ liệu xác thực vì không cần thiết
 * cho việc xây dựng kế hoạch tập luyện và dinh dưỡng.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInputUserSnapshot {

    private String fullName;
}