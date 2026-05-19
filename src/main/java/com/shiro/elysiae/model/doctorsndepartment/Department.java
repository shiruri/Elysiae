package com.shiro.elysiae.model.doctorsndepartment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "departments",
        indexes = {
                @Index(name = "idx_department_name", columnList = "name")
        }
)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // CORE DATA
    // =========================

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 20)
    private String floor;


    // =========================
    // RELATIONSHIP (optional)
    // =========================
    // Only include if you need reverse navigation

    @OneToMany(mappedBy = "department")
    private List<Doctor> doctors = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}