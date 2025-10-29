package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "multipleerrors")
public class MultipleErrors {

    @Id
    private Long id;
    protected String multipleref;
    protected String ethoscaseref;
    protected String description;

    public String toString() {
        return "Ethos Case Reference: '" + this.ethoscaseref
            + "', Description: '" + this.description + "'";
    }

}
