package com.project.CrisisGrid.resource_service.entity;

import com.project.CrisisGrid.resource_service.enums.AllocationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resource_allocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "crisis_id", nullable = false)
    private UUID crisisId;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @CreationTimestamp
    @Column(name = "allocated_at", nullable = false, updatable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocated_by", nullable = false)
    private AllocationType allocatedBy;

    @Column(name = "allocated_by_user_id")
    private UUID allocatedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}