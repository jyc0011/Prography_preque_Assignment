package com.prography.demo.domain;

import com.prography.demo.domain.entity.BaseEntity;
import com.prography.demo.domain.enumType.TeamType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@AttributeOverride(
        name = "updated_at",
        column = @Column(name = "updated_at", insertable = true, updatable = true)
)
public class UserRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "team")
    private TeamType team;
}