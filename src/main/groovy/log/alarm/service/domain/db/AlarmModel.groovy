package log.alarm.service.domain.db

import grails.gorm.annotation.Entity

@Entity
class AlarmModel {

    String name

    String severity

    Status status

    String comment

    Long timestamp

    String customerID

    List<String> logIDs

    Map toReturn() {
        Map ret = [
                "id" : id,
                "name" : name,
                "severity" : severity,
                "status" : status.toString(),
                "timestamp" : new Date(timestamp),
                "customer_id" : customerID,
                "comment" : comment,
                "log_ids" : logIDs
        ]
    }

    static enum Status {

        NEW("NEW"),
        RESOLVED("RESOLVED"),
        ESCALATED("ESCALATED")

        private String stat

        Status(String status) {
            this.stat = status.toUpperCase()
        }

    }

}
