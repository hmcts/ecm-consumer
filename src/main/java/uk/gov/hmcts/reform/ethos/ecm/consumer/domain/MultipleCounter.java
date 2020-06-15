package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import javax.persistence.*;

@Entity
@Table(name = "multipleCounter")
public class MultipleCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    protected String multipleRef;
    protected Integer counter;
}
