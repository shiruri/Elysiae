package com.shiro.elysiae.model.doctorsndepartment;

import com.shiro.elysiae.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "doctors",
        indexes = {
                @Index(name = "idx_doctors_department", columnList = "department_id"),
                @Index(name = "idx_doctors_specialization", columnList = "specialization"),
                @Index(name = "idx_doctors_last_first", columnList = "last_name, first_name")
        }
)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // RELATIONSHIPS
    // =========================================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;


    // =========================================================
    // DOCTOR INFO
    // =========================================================

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 150)
    private String specialization;

    @Column(name = "license_number", unique = true, length = 80)
    private String licenseNumber;

    @Column(length = 20)
    private String phone;
}