package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import javax.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "multipleerrors")
public class MultipleErrors {

    @Id
    protected String multipleref;
    protected String ethoscaseref;
    protected String description;

//    public MultipleErrors(String multipleref, String ethoscaseref, String description) {
//        this.multipleref = multipleref;
//        this.ethoscaseref = ethoscaseref;
//        this.description = description;
//    }
}
