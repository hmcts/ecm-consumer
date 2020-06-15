package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import javax.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "multipleerrors")
public class MultipleErrors {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    protected String multipleRef;
    protected String ethosCaseRef;
    protected String description;

    public MultipleErrors(String multipleRef, String ethosCaseRef, String description) {
        this.multipleRef = multipleRef;
        this.ethosCaseRef = ethosCaseRef;
        this.description = description;
    }
}
