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
import log.alarm.service.service.AlarmService
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

    @Shared @Inject
    AlarmService alarmService

    void setupSpec() {
        embeddedServer = ApplicationContext.build().packages("log.alarm.service").run(EmbeddedServer)
        client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.URL)
        alarmService = embeddedServer.applicationContext.getBean(AlarmService)
    }

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
        HttpResponse response = client.toBlocking().exchange(request, Map)

        then:
        response.status == HttpStatus.CREATED
        response.body().name == expectedReply.name
        response.body().severity == expectedReply.severity
        response.body().status == expectedReply.status.toString()
        response.body().timestamp != null
        response.body().customer_id == expectedReply.customerID
        response.body().log_ids == expectedReply.logIDs

        when:
        req.logID = "New Mega Secret"
        HttpRequest requestNew = HttpRequest.POST("/alarms", req)
        HttpResponse responseNew = client.toBlocking().exchange(requestNew, Map)

        then:
        responseNew.status == HttpStatus.OK
        responseNew.body().name == expectedReply.name
        responseNew.body().severity == expectedReply.severity
        responseNew.body().status == expectedReply.status.toString()
        responseNew.body().timestamp != null
        responseNew.body().customer_id == expectedReply.customerID
        responseNew.body().log_ids == expectedReply.logIDs + req.logID
    }

    void "test all logs can be retrieved"() {
        setup:
        (1..9).each {
            alarmService.saveAlarm(
                    new SaveAlarm(name: it.toString(), severity: "not bad",
                                  customerID: "new customer", logID: "log${it*42}"
                    )
            )
        }

        when:
        HttpRequest request = HttpRequest.GET("/alarms")
        HttpResponse response = client.toBlocking().exchange(request, Map)

        then:
        response.status == HttpStatus.OK
        response.body().num_results == 10
        response.body().content[0].name == "MyAlarm" // from previous test
        response.body().content[0].log_ids == ["Mega Secret", "New Mega Secret"] // from previous test
        response.body().content[1].name == 1.toString()
        response.body().content[1].log_ids == ["log42"]
        response.body().content[9].name == 9.toString()
        response.body().content[9].log_ids == ["log378"]
        response.body().content.size() == 10
    }

    void "test alarm can be retrieved by id"() {
        setup:
        String name = "MyAlarm" // from previous test
        List<String> logIDs = ["Mega Secret", "New Mega Secret"] // from previous test

        when:
        HttpRequest request = HttpRequest.GET("/alarms")
        HttpResponse response = client.toBlocking().exchange(request, Map)
        Long id = response.body().content[0].id

        then:
        response.status == HttpStatus.OK
        response.body().num_results == 10
        response.body().content[0].name == name
        response.body().content[0].log_ids == logIDs

        when:
        HttpRequest requestSingle = HttpRequest.GET("/alarms/$id")
        HttpResponse responseSingle = client.toBlocking().exchange(requestSingle, Map)

        then:
        responseSingle.status == HttpStatus.OK
        responseSingle.body().id == id
        responseSingle.body().name == name
        responseSingle.body().log_ids == logIDs
    }

    void "test alarms can be retrieved with query parameters"() {
        setup:
        String statusR = AlarmModel.Status.RESOLVED.toString()
        String statusN = AlarmModel.Status.NEW.toString()
        String customerID = "new customer"
        String customerIDURL = "new%20customer"

        when:
        HttpRequest request = HttpRequest.GET("/alarms?customer_id=$customerIDURL")
        HttpResponse response = client.toBlocking().exchange(request, Map)

        then:
        response.status == HttpStatus.OK
        response.body().num_results == 9
        response.body().content.each { assert it.severity == 'not bad' }

        when:
        HttpRequest requestR = HttpRequest.GET("/alarms?customer_id=$customerIDURL&status=$statusR")
        HttpResponse responseR = client.toBlocking().exchange(requestR, Map)

        then:
        responseR.status == HttpStatus.OK
        responseR.body().num_results == 0

        when:
        HttpRequest requestN = HttpRequest.GET("/alarms?customer_id=$customerIDURL&status=$statusN")
        HttpResponse responseN = client.toBlocking().exchange(requestN, Map)

        then:
        responseN.status == HttpStatus.OK
        responseN.body().num_results == 9
        responseN.body().content.each { assert it.severity == 'not bad' }
        responseN.body().content.each { assert it.customer_id == customerID }
    }

    // FOLLOWING, PUTTING ALL TESTS HERE, DUE TO MONGO CONFIGURATION ISSUE

    void "service test new alarm is created and existing alarm has log id appended"() {
        setup:
        String name = "My Service Alarm"
        String severity = "I've Got A Bad Feeling About This One"
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
        AlarmService.ServiceResult saved = alarmService.saveAlarm(req)

        then:
        saved.code == 201
        saved.body.name == expectedReply.name
        saved.body.severity == expectedReply.severity
        saved.body.status == expectedReply.status.toString()
        saved.body.timestamp != null
        saved.body.customer_id == expectedReply.customerID
        saved.body.log_ids == expectedReply.logIDs

        when:
        req.logID = "New Mega Secret"
        AlarmService.ServiceResult saveAgain = alarmService.saveAlarm(req)

        then:
        saveAgain.code == 200
        saveAgain.body.name == expectedReply.name
        saveAgain.body.severity == expectedReply.severity
        saveAgain.body.status == expectedReply.status.toString()
        saveAgain.body.timestamp != null
        saveAgain.body.customer_id == expectedReply.customerID
        saveAgain.body.log_ids == expectedReply.logIDs + req.logID
    }

    void "service test all logs can be retrieved"() {
        setup:
        (1..9).each {
            alarmService.saveAlarm(
                    new SaveAlarm(name: it.toString(), severity: "bad",
                            customerID: "test customer", logID: "log${it*42}"
                    )
            )
        }

        when:
        AlarmService.ServiceResult result = alarmService.getAll()

        then:
        result.code == 200
        result.body.num_results == 20
        result.body.content[10].name == "My Service Alarm" // from previous test
        result.body.content[10].log_ids == ["Mega Secret", "New Mega Secret"] // from previous test
        result.body.content[11].name == 1.toString()
        result.body.content[11].log_ids == ["log42"]
        result.body.content[19].name == 9.toString()
        result.body.content[19].log_ids == ["log378"]
        result.body.content.size() == 20
    }

    void "service test alarm can be retrieved by id"() {
        setup:
        String name = "My Service Alarm" // from previous test
        List<String> logIDs = ["Mega Secret", "New Mega Secret"] // from previous test

        when:
        AlarmService.ServiceResult retrieved = alarmService.getAll()
        Long id = retrieved.body.content[10].id

        then:
        retrieved.code == 200
        retrieved.body.num_results == 20
        retrieved.body.content[10].name == name
        retrieved.body.content[10].log_ids == logIDs

        when:
        AlarmService.ServiceResult retrievedSingle = alarmService.getByAlarmID(id)

        then:
        retrievedSingle.code == 200
        retrievedSingle.body.id == id
        retrievedSingle.body.name == name
        retrievedSingle.body.log_ids == logIDs
    }

    void "service test alarms can be retrieved with query parameters"() {
        setup:
        String statusR = AlarmModel.Status.RESOLVED.toString()
        String statusN = AlarmModel.Status.NEW.toString()
        String customerID = "test customer"

        when:
        AlarmService.ServiceResult retrievedID = alarmService.getByCustomerID(customerID)

        then:
        retrievedID.code == 200
        retrievedID.body.num_results == 9
        retrievedID.body.content.each { assert it.severity == 'bad' }

        when:
        AlarmService.ServiceResult idAndStatusR = alarmService.getByCustomerAndStatus(customerID, statusR)

        then:
        idAndStatusR.code == 200
        idAndStatusR.body.num_results == 0

        when:
        AlarmService.ServiceResult idAndStatusN = alarmService.getByCustomerAndStatus(customerID, statusN)

        then:
        idAndStatusN.code == 200
        idAndStatusN.body.num_results == 9
        idAndStatusN.body.content.each { assert it.severity == 'bad' }
        idAndStatusN.body.content.each { assert it.customer_id == customerID }
    }
}
