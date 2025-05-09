package com.prography.demo.domain;

import com.prography.demo.domain.entity.BaseEntity;
import com.prography.demo.domain.enumType.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(
        name = "updated_at",
        column = @Column(name = "updated_at", insertable = true, updatable = true)
)
public class Users extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "fakerId")
    private Integer fakerId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private UserStatus status;

}