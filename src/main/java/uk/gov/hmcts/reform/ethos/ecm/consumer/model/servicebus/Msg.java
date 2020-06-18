package uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
@Data
public abstract class Msg {

    @JsonProperty("id")
    String msgId;

    @JsonProperty("jurisdiction")
    String jurisdiction;

    @JsonProperty("caseTypeId")
    String caseTypeId;

    @JsonProperty("multipleRef")
    String multipleRef;

    @JsonProperty("totalCases")
    String totalCases;

    @JsonProperty("username")
    String username;

    @JsonProperty("updateType")
    String updateType;

    @JsonProperty("updateData")
    UpdateData updateData;

}
