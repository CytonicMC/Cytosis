package net.cytonic.cytosis.report;

import java.time.Instant;
import java.util.UUID;

import io.ebean.Model;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.WhenCreated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * DO NOT USE THIS CLASS. THIS CLASS IS TO BE EXCLUSIVELY USED BY THE {@link ReportManager} OR {@link Report} CLASSES.
 */
@Setter
@Getter
@Entity
@Internal
@Table(name = "cytonic_reports")
public class ReportEntity extends Model {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String type;

    @WhenCreated
    private Instant submittedAt;

    @Column(nullable = false)
    private UUID reporter;

    @Column(nullable = false)
    private UUID player;

    @DbDefault("false")
    private boolean resolved = false;

    @DbJsonB
    private String context;
}
