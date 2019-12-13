package log.alarm.service.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import log.alarm.service.domain.model.SaveAlarm
import log.alarm.service.service.AlarmService

import javax.inject.Inject
import javax.validation.Valid

@Validated
@Controller("/alarms")
class AlarmController {

    private final AlarmService alarmService

    AlarmController(AlarmService service) {
        alarmService = service
    }

    @Post("/")
    HttpResponse saveAlarm(@Valid @Body SaveAlarm saveAlarm) {
        AlarmService.ServiceResult result = alarmService.saveAlarm(saveAlarm)

        switch (result.code) {
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

    @Get("/")
    HttpResponse getAllAlarms() {
        AlarmService.ServiceResult result = alarmService.getAll()

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