package uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class CreateUpdatesMsg implements Msg {

    @JsonProperty("id")
    private String msgId;

    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("multipleRef")
    private String multipleRef;

    @JsonProperty("ethosCaseRefCollection")
    private List<String> ethosCaseRefCollection;

    @JsonProperty("totalCases")
    private String totalCases;

    @JsonProperty("username")
    private String username;

    @JsonProperty("label")
    private String label;

    @Override
    @JsonIgnore
    public String getMsgId() {
        return msgId;
    }

    @Override
    @JsonIgnore
    public String getLabel() {
        return label;
    }
}
