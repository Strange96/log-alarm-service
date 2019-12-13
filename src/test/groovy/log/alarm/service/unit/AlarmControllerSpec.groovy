package log.alarm.service.unit

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import log.alarm.service.domain.db.AlarmModel
import log.alarm.service.domain.model.SaveAlarm
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest
class AlarmControllerSpec extends Specification {

    @Shared @Inject
    EmbeddedServer embeddedServer

    @Shared @AutoCleanup @Inject @Client("/")
    RxHttpClient client

    void "test new alarm is created and existing alarm has log id appended"() {
        setup:
        String name = "MyAlarm"
        String severity = "Very Bad"
        AlarmModel.Status statusNew = AlarmModel.Status.NEW
        String customerID = "Chuck f'ing Norris"
        String logID = "Mega Secret"
        SaveAlarm req = new SaveAlarm(
                name: name, severity: severity, customerID: customerID, logID: logID
        )
        AlarmModel expectedReply = new AlarmModel(
                name: name, severity: severity, status: statusNew, timestamp: new Date().time,
                customerID: customerID, comment: "", logIDs: [logID]
        )

        when:
        HttpRequest request = HttpRequest.POST("/alarms", req)
        HttpResponse response = client.toBlocking().retrieve(request, Map)

        then:
        response.status == HttpStatus.CREATED
        response.body().name == expectedReply.name
        response.body().severity == expectedReply.severity
        response.body().status == expectedReply.status
        response.body().timestamp != null
        response.body().customer_id == expectedReply.customerID
        response.body().log_ids == expectedReply.logIDs

        when:
        req.logID = "New Mega Secret"
        HttpRequest requestNew = HttpRequest.POST("/alarms", req)
        HttpResponse responseNew = client.toBlocking().retrieve(requestNew, Map)

        then:
        responseNew.status == HttpStatus.CREATED
        responseNew.body().name == expectedReply.name
        responseNew.body().severity == expectedReply.severity
        responseNew.body().status == expectedReply.status
        responseNew.body().timestamp != null
        responseNew.body().customer_id == expectedReply.customerID
        responseNew.body().log_ids == expectedReply.logIDs + req.logID
    }
}
