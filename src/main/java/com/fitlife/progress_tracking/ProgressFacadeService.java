package com.fitlife.progress_tracking;

import com.fitlife.progress_tracking.dto.MemberProgressSummaryResponse;
import org.springframework.transaction.annotation.Transactional;

public interface ProgressFacadeService {
    @Transactional(readOnly = true)
    MemberProgressSummaryResponse getMyProgress(Long memberId);
}