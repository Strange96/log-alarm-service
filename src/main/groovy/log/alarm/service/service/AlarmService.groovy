package log.alarm.service.service

import grails.gorm.transactions.Transactional
import log.alarm.service.domain.db.AlarmModel
import log.alarm.service.domain.model.SaveAlarm

import javax.inject.Singleton

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
            return new ServiceResult(code: 201, body: logAlarm.toReturn())
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

    ServiceResult getAll() {
        List<AlarmModel> all = AlarmModel.list()

        if (!all) {
            return new ServiceResult(code: 500)
        }

        List<Map> returnList = all.collect { it.toReturn() }

        new ServiceResult(code: 200, body: ["num_results" : returnList.size(), "content" : returnList])
    }

    static class ServiceResult {

        int code
        Map body

    }

}
