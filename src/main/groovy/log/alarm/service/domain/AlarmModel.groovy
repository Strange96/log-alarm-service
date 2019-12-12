package log.alarm.service.domain

import grails.gorm.annotation.Entity

@Entity
class AlarmModel {

    String time

    String name

    List<String> logIDs

}
