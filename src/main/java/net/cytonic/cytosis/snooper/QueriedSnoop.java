package net.cytonic.cytosis.snooper;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@Table(name = "cytonic_snoops")
@NoArgsConstructor
public class QueriedSnoop {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "channel")
    private String channel;
    @Column(name = "target")
    private byte target;
    @Column(name = "content")
    private String content;
    @Column(name = "created")
    @CreationTimestamp
    private Instant created;

    public QueriedSnoop(String channel, byte target, String content) {
        this.channel = channel;
        this.target = target;
        this.content = content;
    }
}
