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

    @JsonProperty("multipleRef")
    private String multipleRef;

    @JsonProperty("ethosCaseReference")
    private String ethosCaseReference;

    @JsonProperty("totalCases")
    private String totalCases;

    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("username")
    private String username;

    @Override
    @JsonIgnore
    public String getMsgId() {
        return msgId;
    }

    @Override
    public String getLabel() {
        return "UpdateCaseMsgTest";
    }
}
