package log.alarm.service.integration

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import log.alarm.service.domain.db.AlarmModel
import log.alarm.service.domain.model.SaveAlarm
import log.alarm.service.service.AlarmService
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Shared

import javax.inject.Inject

@Ignore
@MicronautTest
class AlarmServiceSpec extends Specification {

    @Shared @Inject
    EmbeddedServer embeddedServer

    @Shared @Inject @AutoCleanup
    AlarmService alarmService

    void setupSpec() {
        embeddedServer = ApplicationContext.build().packages("log.alarm.service").run(EmbeddedServer)
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
        AlarmService.ServiceResult result = alarmService.getAll()

        then:
        result.code == 200
        result.body.num_results == 10
        result.body.content[0].name == "MyAlarm" // from previous test
        result.body.content[0].log_ids == ["Mega Secret", "New Mega Secret"] // from previous test
        result.body.content[1].name == 1.toString()
        result.body.content[1].log_ids == ["log42"]
        result.body.content[9].name == 9.toString()
        result.body.content[9].log_ids == ["log378"]
        result.body.content.size() == 10
    }

    void "test alarm can be retrieved by id"() {
        setup:
        String name = "MyAlarm" // from previous test
        List<String> logIDs = ["Mega Secret", "New Mega Secret"] // from previous test

        when:
        AlarmService.ServiceResult retrieved = alarmService.getAll()
        Long id = retrieved.body.content[0].id

        then:
        retrieved.code == 200
        retrieved.body.num_results == 10
        retrieved.body.content[0].name == name
        retrieved.body.content[0].log_ids == logIDs

        when:
        AlarmService.ServiceResult retrievedSingle = alarmService.getByAlarmID(id)

        then:
        retrievedSingle.code == 200
        retrievedSingle.body.id == id
        retrievedSingle.body.name == name
        retrievedSingle.body.log_ids == logIDs
    }

    void "test alarms can be retrieved with query parameters"() {
        setup:
        String statusR = AlarmModel.Status.RESOLVED.toString()
        String statusN = AlarmModel.Status.NEW.toString()
        String customerID = "new customer"

        when:
        AlarmService.ServiceResult retrievedID = alarmService.getByCustomerID(customerID)

        then:
        retrievedID.code == 200
        retrievedID.body.num_results == 9
        retrievedID.body.content.each { assert it.severity == 'not bad' }

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
        idAndStatusN.body.content.each { assert it.severity == 'not bad' }
        idAndStatusN.body.content.each { assert it.customer_id == customerID }
    }
}
