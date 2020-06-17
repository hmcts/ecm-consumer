package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "multipleerrors")
public class MultipleErrors {

    @Id
    protected String multipleref;
    protected String ethoscaseref;
    protected String description;

    public String toString1() {
        return "MultipleRef: '" + this.multipleref
            + "', EthosCaseRef: '" + this.ethoscaseref
            + "', Description: '" + this.description + "'"
            + System.lineSeparator();
    }

}
