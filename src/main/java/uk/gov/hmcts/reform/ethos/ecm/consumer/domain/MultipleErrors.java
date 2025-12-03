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
    private String multipleref;
    private String ethoscaseref;
    private String description;

    public String toString() {
        return "MultipleRef: " + multipleref + "\n\nEthosCaseRef: " + ethoscaseref + " \n\nDescription: "
            + description + "\n\n";
    }

}
