package com.prography.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Where(clause = "is_deleted = false")
public abstract class BaseEntity {
    // 데이터가 저장되는 시점에 따라 createdAt과 updatedAt을 저장합니다.
    @CreationTimestamp
    @Column(updatable = false) // 수정시 관여하지 않음
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column(insertable = false) // 삽입시 관여하지 않음
    private LocalDateTime updated_at;
}