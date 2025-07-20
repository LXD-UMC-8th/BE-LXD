package org.lxdproject.lxd.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.common.entity.enums.ImageDir;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ImageDir dir;
}

