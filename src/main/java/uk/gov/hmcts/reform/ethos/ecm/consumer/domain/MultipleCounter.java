package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@NoArgsConstructor
@Table(name = "multiplecounter")
public class MultipleCounter {

    @Id
    protected String multipleref;
    protected Integer counter;
}
