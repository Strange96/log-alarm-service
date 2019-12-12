package log.alarm.service.domain

import grails.gorm.annotation.Entity

@Entity
class AlarmModel {

    String name

    String severity

    Date timestamp

    List<String> logIDs

}
