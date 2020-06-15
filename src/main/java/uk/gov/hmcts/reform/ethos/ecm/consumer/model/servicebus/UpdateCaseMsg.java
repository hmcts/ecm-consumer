package uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UpdateCaseMsg implements Msg {

    @JsonProperty("id")
    private String msgId;

    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("multipleRef")
    private String multipleRef;

    @JsonProperty("ethosCaseReference")
    private String ethosCaseReference;

    @JsonProperty("totalCases")
    private String totalCases;

    @JsonProperty("username")
    private String username;

    @JsonProperty("label")
    private String label;

    @JsonProperty("updateData")
    private UpdateData updateData;

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
