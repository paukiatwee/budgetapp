package io.budgetapp.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "ledger_types")
public class LedgerType implements Serializable {

    private static final long serialVersionUID = -7580231307267509312L;

    private Long id;
    private Date createdAt;
    private List<Ledger> ledgers;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, nullable = false, updatable = false)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JoinColumn(updatable = false)
    @OneToMany()
    public List<Ledger> getLedgers() {
        return ledgers;
    }

    public void setLedgers(List<Ledger> ledgers) {
        this.ledgers = ledgers;
    }

    @Override
    public String toString() {
        return "LedgerType{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", ledgers=" + ledgers +
                '}';
    }
}
