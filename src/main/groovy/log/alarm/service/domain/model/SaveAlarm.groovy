package log.alarm.service.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

class SaveAlarm {

    String name

    String severity

    @JsonProperty("customer_id")
    String customerID

    @JsonProperty("log_id")
    String logID

}
