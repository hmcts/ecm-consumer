package uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UpdateData {

    @JsonProperty("lead")
    private String lead;

    @JsonProperty("claimantRep")
    private String claimantRep;

}
