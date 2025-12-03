package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "multiplecounter")
public class MultipleCounter {

    @Id
    private String multipleref;
    private Integer counter;
}
