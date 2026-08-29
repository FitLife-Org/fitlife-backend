package com.fitlife.workout.enums;

public enum WorkoutPlanSourceType {

    AI_GENERATED,
    TRAINER_CREATED,
    MEMBER_CREATED,
    MANUAL;

    /**
     * Member được chỉnh nội dung plan do:
     * - AI tạo và member đã apply
     * - Member tự tạo
     * - Legacy/manual
     *
     * Trainer-created plan là read-only đối với Member.
     */
    public boolean isEditableByMember() {
        return this == AI_GENERATED
                || this == MEMBER_CREATED
                || this == MANUAL;
    }
}