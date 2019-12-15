package log.alarm.service.domain.model

import javax.annotation.Nullable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class UpdateAlarm {

    @NotNull
    @NotBlank
    String status

    @Nullable
    String comment
}
