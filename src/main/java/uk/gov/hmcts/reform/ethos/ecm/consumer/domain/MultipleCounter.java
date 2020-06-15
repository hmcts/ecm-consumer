package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "multipleCounter")
public class MultipleCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    protected String multipleRef;
    protected Integer counter;
}
