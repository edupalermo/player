package org.palermo.totalbattle.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.palermo.totalbattle.player.Clan;
import org.palermo.totalbattle.player.PlayerName;
import org.palermo.totalbattle.player.state.location.Crypt;
import org.palermo.totalbattle.player.state.location.Location;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "player")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // works with BIGSERIAL in PostgreSQL
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 255)
    private PlayerName playerName;

    @Setter
    @Column(name = "username", nullable = false, length = 255)
    private String username;
    
    @Setter
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Setter
    @Column(name = "profile_folder", nullable = false, length = 255)
    private String profileFolder;

    @Setter
    @Column(name = "priority", nullable = false)
    private int priority;

    @Setter
    @Column(name = "lock", nullable = false)
    private boolean lock;

    @Setter
    @Column(name = "halt", nullable = false)
    private boolean halt;

    @Setter
    @Column(name = "has_helen", nullable = false)
    private boolean hasHelen;    

    @Setter @Column(nullable = false)
    private long lumber = 0L;

    @Setter @Column(nullable = false)
    private long iron = 0L;

    @Setter @Column(nullable = false)
    private long stone = 0L;

    @Setter @Column(nullable = false)
    private long silver = 0L;

    @Setter @Column(name = "common_tar", nullable = false)
    private long commonTar = 0L;

    @Setter @Column(name = "target_lumber", nullable = false)
    private long targetLumber = 0L;

    @Setter @Column(name = "target_iron", nullable = false)
    private long targetIron = 0L;

    @Setter @Column(name = "target_stone", nullable = false)
    private long targetStone = 0L;

    @Setter @Column(name = "target_silver", nullable = false)
    private long targetSilver = 0L;

    @Setter @Column(name = "last_logout")
    private LocalDateTime lastLogout;

    @Version @Column(name = "version")
    private LocalDateTime version;

    @Setter
    @Column(name = "playing", nullable = false)
    private boolean playing;

    @Setter @Column(nullable = false)
    private long leadership = 0L;

    @Setter @Column(nullable = false)
    private long dominance = 0L;

    @Setter @Column(nullable = false)
    private long authority = 0L;

    @Setter @Column(name = "attack_waves", nullable = false)
    private int attackWaves = 0;

    @Setter
    @Column(name = "position_x", nullable = false)
    private int positionX = 0;

    @Setter
    @Column(name = "position_y", nullable = false)
    private int positionY = 0;

    @Setter
    @Column(name = "common_crypt_level", nullable = false)
    private int commonCryptLevel = 0;

    @Setter
    @Column(name = "rare_crypt_level", nullable = false)
    private int rareCryptLevel = 0;

    @Setter
    @Column(name = "common_tar_required", nullable = false)
    private long commonTarRequired = 0L;

    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Crypt commonCryptExploringLocation;
    
    @Setter
    @Column(name = "rare_tar_required", nullable = false)
    private long rareTarRequired = 0L;

    @Setter
    @Column(name = "citadel_level", nullable = false)
    private int citadelLevel = 0;

    @Setter
    @Column(name = "kingdom", nullable = false, length = 255)
    private int kingdom;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "clan", nullable = false, length = 255)
    private Clan clan;

    @OneToMany(mappedBy = "playerEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitEntity> unitEntities = new ArrayList<>();
    
}