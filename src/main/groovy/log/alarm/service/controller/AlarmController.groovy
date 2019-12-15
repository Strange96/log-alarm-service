package log.alarm.service.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import log.alarm.service.domain.model.SaveAlarm
import log.alarm.service.domain.model.UpdateAlarm
import log.alarm.service.service.AlarmService
import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Validated
@Controller("/alarms")
class AlarmController {

    @Inject
    private final AlarmService alarmService

    AlarmController(AlarmService service) {
        alarmService = service
    }

    @Post("/")
    HttpResponse saveAlarm(@Valid @Body SaveAlarm saveAlarm) {
        AlarmService.ServiceResult result = alarmService.saveAlarm(saveAlarm)

        switch (result.code) {
            case 200:
                return HttpResponse.ok(result.body)

            case 201:
                return HttpResponse.created(result.body)

            case 400:
                return HttpResponse.badRequest()

            case 404:
                return HttpResponse.notFound()

            case 500:
            default:
                HttpResponse.serverError()
        }
    }

    @Get("/{?customer_id,severity,status}")
    HttpResponse getAllAlarms(@Nullable @QueryValue("customer_id") String customerID,
                              @Nullable @QueryValue("severity") String severity,
                              @Nullable @QueryValue("status") String status) {
        AlarmService.ServiceResult result
        if (customerID && severity && !status) { result = alarmService.getByCustomerAndSeverity(customerID, severity) }
        else if (customerID && status && !severity) { result = alarmService.getByCustomerAndStatus(customerID, status) }
        else if (customerID && !severity && !status) { result = alarmService.getByCustomerID(customerID) }
        else { result = alarmService.getAll() }

        switch (result.code) {
            case 200:
                return HttpResponse.ok(result.body)

            case 400:
                return HttpResponse.badRequest()

            case 404:
                return HttpResponse.notFound()

            case 500:
            default:
                HttpResponse.serverError()
        }
    }

    @Get("/{alarm_id}")
    HttpResponse getAlarmById(@NotNull @PathVariable(value = "alarm_id") Long alarmID) {
        AlarmService.ServiceResult result = alarmService.getByAlarmID(alarmID)

        switch (result.code) {
            case 200:
                return HttpResponse.ok(result.body)

            case 400:
                return HttpResponse.badRequest()

            case 404:
                return HttpResponse.notFound()

            case 500:
            default:
                HttpResponse.serverError()
        }
    }

    @Patch("/{alarm_id}")
    HttpResponse updateAlarmByID(@NotNull @PathVariable(value = "alarm_id") Long alarmID,
                                 @Valid @Body UpdateAlarm updateBody) {
        AlarmService.ServiceResult result = alarmService.updateAlarmByID(alarmID, updateBody)

        switch (result.code) {
            case 200:
                return HttpResponse.ok(result.body)

            case 400:
                return HttpResponse.badRequest()

            case 404:
                return HttpResponse.notFound()

            case 500:
            default:
                HttpResponse.serverError()
        }
    }

    @Error(status = HttpStatus.BAD_REQUEST)
    HttpResponse badReq() {
        HttpResponse.badRequest(["error" : "invalid request"])
    }

    @Error(status = HttpStatus.NOT_FOUND)
    HttpResponse notFound() {
        HttpResponse.notFound(["error" : "resource not found"])
    }

    @Error(status = HttpStatus.INTERNAL_SERVER_ERROR)
    HttpResponse serverError() {
        HttpResponse.serverError(["error" : "internal error. something went wrong"])
    }

}