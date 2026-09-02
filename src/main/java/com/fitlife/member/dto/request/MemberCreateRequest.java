package com.fitlife.member.dto.request;

import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.member.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberCreateRequest {

    private static final String OPTIONAL_VN_PHONE_REGEX =
            "^$|^(0|\\+84)[0-9]{9,10}$";

    @NotBlank(
            message = "USERNAME_REQUIRED"
    )
    @Size(
            max = 100,
            message = "USERNAME_TOO_LONG"
    )
    private String username;

    @Email(
            message = "INVALID_EMAIL"
    )
    @NotBlank(
            message = "EMAIL_REQUIRED"
    )
    @Size(
            max = 150,
            message = "EMAIL_TOO_LONG"
    )
    private String email;

    private String password;

    @NotBlank(
            message = "FULLNAME_REQUIRED"
    )
    @Size(
            max = 150,
            message = "FULLNAME_TOO_LONG"
    )
    private String fullName;

    /**
     * Không bắt buộc.
     *
     * Chấp nhận:
     * - null
     * - chuỗi rỗng
     * - số bắt đầu bằng 0
     * - số bắt đầu bằng +84
     */
    @Pattern(
            regexp =
                    OPTIONAL_VN_PHONE_REGEX,
            message = "INVALID_PHONE"
    )
    private String phone;

    private Gender gender;

    @Past(
            message =
                    "DATE_OF_BIRTH_MUST_BE_IN_PAST"
    )
    private LocalDate dateOfBirth;

    @Size(
            max = 255,
            message = "ADDRESS_TOO_LONG"
    )
    private String address;

    @Size(
            max = 100,
            message =
                    "EMERGENCY_CONTACT_NAME_TOO_LONG"
    )
    private String emergencyContactName;

    /**
     * Không bắt buộc.
     *
     * Nếu có giá trị thì phải là
     * số điện thoại Việt Nam hợp lệ.
     */
    @Pattern(
            regexp =
                    OPTIONAL_VN_PHONE_REGEX,
            message =
                    "INVALID_EMERGENCY_CONTACT_PHONE"
    )
    private String emergencyContactPhone;

    private FitnessGoal fitnessGoal;

    @Size(
            max = 1000,
            message = "HEALTH_NOTE_TOO_LONG"
    )
    private String healthNote;
}