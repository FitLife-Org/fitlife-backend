package com.fitlife.member.dto.request;

import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.member.enums.Gender;
import com.fitlife.member.enums.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberUpdateRequest {

    @Size(max = 150, message = "FULLNAME_TOO_LONG")
    private String fullName;

    @Email(message = "INVALID_EMAIL")
    @Size(max = 150, message = "EMAIL_TOO_LONG")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "INVALID_PHONE")
    private String phone;

    private Gender gender;

    @Past(message = "DATE_OF_BIRTH_MUST_BE_IN_PAST")
    private LocalDate dateOfBirth;

    @Size(max = 255, message = "ADDRESS_TOO_LONG")
    private String address;

    @Size(max = 100, message = "EMERGENCY_CONTACT_NAME_TOO_LONG")
    private String emergencyContactName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "INVALID_EMERGENCY_CONTACT_PHONE")
    private String emergencyContactPhone;

    private FitnessGoal fitnessGoal;

    @Size(max = 1000, message = "HEALTH_NOTE_TOO_LONG")
    private String healthNote;

    private MemberStatus status;
}