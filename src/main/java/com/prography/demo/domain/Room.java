package com.prography.demo.domain;

import com.prography.demo.domain.entity.BaseEntity;
import com.prography.demo.domain.enumType.RoomStatus;
import com.prography.demo.domain.enumType.RoomType;
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
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "host")
    private Integer host;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "room_type")
    private RoomType room_type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private RoomStatus status;
}
