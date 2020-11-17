package com.company.qldp.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "family_member")
@Access(AccessType.FIELD)
public class FamilyMember implements Serializable {
    
    @Id
    @OneToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "person_id", nullable = false)
    private People person;
    
    @Id
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;
    
    @Column(name = "host_relation")
    private String hostRelation;
}
