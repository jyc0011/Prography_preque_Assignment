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
public class UserRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "room_id")
    private Integer room_id;

    @Column(name = "user_id")
    private Integer user_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "team")
    private TeamType team;
}