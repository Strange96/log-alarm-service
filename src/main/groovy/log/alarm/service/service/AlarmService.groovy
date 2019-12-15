package log.alarm.service.service

import grails.gorm.transactions.Transactional
import log.alarm.service.domain.db.AlarmModel
import log.alarm.service.domain.model.SaveAlarm
import log.alarm.service.domain.model.UpdateAlarm

import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Singleton
@Transactional
class AlarmService {

    ServiceResult saveAlarm(SaveAlarm saveAlarm) {
        AlarmModel logAlarm = AlarmModel.findByNameAndSeverityAndCustomerID(
                saveAlarm.name, saveAlarm.severity, saveAlarm.customerID
        )

        Long time = new Date().time

        if (logAlarm) {
            logAlarm.timestamp = time
            logAlarm.logIDs.add(saveAlarm.logID)
            if (logAlarm.hasErrors()) {
                return new ServiceResult(code: 500)
            }
            logAlarm.save()
            return new ServiceResult(code: 200, body: logAlarm.toReturn())
        }

        logAlarm = new AlarmModel(
                name: saveAlarm.name,
                severity: saveAlarm.severity,
                status: AlarmModel.Status.NEW,
                timestamp: time,
                customerID: saveAlarm.customerID,
                comment: "",
                logIDs: [saveAlarm.logID]
        )

        if (logAlarm.hasErrors()) {
            return new ServiceResult(code: 500)
        }

        logAlarm.save()
        new ServiceResult(code: 201, body: logAlarm.toReturn())
    }

    ServiceResult getByCustomerAndStatus(@NotNull @NotBlank String customerID, @NotNull @NotBlank String status) {
        if (!AlarmModel.Status.values().contains(status.toUpperCase())) {
            return new ServiceResult(code: 400)
        }
        AlarmModel.Status stat = AlarmModel.Status.valueOf(status)
        List<AlarmModel> allFound = AlarmModel.findAllByCustomerIDAndStatus(customerID, stat)

        if(!allFound) {
            return new ServiceResult(code: 200, body: ["num_results" : allFound.size(), "content" : null])
        }

        List<Map> returnList = allFound.collect { it.toReturn() }
        new ServiceResult(code: 200, body: ["num_results" : returnList.size(), "content" : returnList])
    }

    ServiceResult getByCustomerAndSeverity(@NotNull @NotBlank String customerID, @NotNull @NotBlank String severity) {
        List<AlarmModel> allFound = AlarmModel.findAllByCustomerIDAndSeverity(customerID, severity)

        if(!allFound) {
            return new ServiceResult(code: 200, body: ["num_results" : allFound.size(), "content" : null])
        }

        List<Map> returnList = allFound.collect { it.toReturn() }
        new ServiceResult(code: 200, body: ["num_results" : returnList.size(), "content" : returnList])
    }

    ServiceResult getByCustomerID(@NotNull @NotBlank String customerID) {
        List<AlarmModel> allForCustomer = AlarmModel.findAllByCustomerID(customerID)

        if(!allForCustomer) {
            return new ServiceResult(code: 200, body: ["num_results" : allForCustomer.size(), "content" : null])
        }

        List<Map> returnList = allForCustomer.collect { it.toReturn() }
        new ServiceResult(code: 200, body: ["num_results" : returnList.size(), "content" : returnList])
    }

    ServiceResult getAll() {
        List<AlarmModel> all = AlarmModel.list()

        if (!all) {
            return new ServiceResult(code: 500)
        }

        List<Map> returnList = all.collect { it.toReturn() }

        new ServiceResult(code: 200, body: ["num_results" : returnList.size(), "content" : returnList])
    }

    ServiceResult getByAlarmID(@NotNull Long alarmID) {
        AlarmModel foundAlarm = AlarmModel.findById(alarmID)

        if (!foundAlarm) {
            return new ServiceResult(code: 404)
        }

        new ServiceResult(code: 200, body: foundAlarm.toReturn())
    }

    ServiceResult updateAlarmByID(@NotNull Long alarmID, UpdateAlarm body) {
        if (!AlarmModel.Status.values().contains(body.status.toUpperCase())) {
            return new ServiceResult(code: 400)
        }

        AlarmModel foundAlarm = AlarmModel.findById(alarmID)

        if (!foundAlarm) {
            return new ServiceResult(code: 404)
        }

        foundAlarm.status = AlarmModel.Status.valueOf(body.status)
        foundAlarm.comment = body.comment

        if (foundAlarm.hasErrors()) {
            return new ServiceResult(code: 500)
        }

        foundAlarm.save()
        new ServiceResult(code: 200, body: foundAlarm.toReturn())
    }

    static class ServiceResult {

        int code
        Map body

    }

}
