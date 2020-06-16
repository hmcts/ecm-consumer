package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import javax.persistence.*;

@Entity
@Table(name = "multipleerrors")
public class MultipleErrors {

    @Id
    protected String multipleref;
    protected String ethoscaseref;
    protected String description;

}
