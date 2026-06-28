package com.project.CrisisGrid.crisis_service.entity;



import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import com.project.CrisisGrid.crisis_service.enums.CrisisType;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crisis_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrisisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String address;

    @Column(name = "reported_by", nullable = false)
    private UUID reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrisisStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "crisis_type")
    private CrisisType crisisType;

    @Column(name = "severity_score")
    private Integer severityScore;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
