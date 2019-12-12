package log.alarm.service.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus


@Controller("/alarm")
class AlarmController {

    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }
}