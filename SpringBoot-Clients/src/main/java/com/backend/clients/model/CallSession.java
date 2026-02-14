package com.backend.clients.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CallSession {
    private String uniqueId;
    private LocalDateTime startTime;
    private String callerId;
    private String selectedOption;
    private boolean validated;

    public CallSession(String uniqueId, LocalDateTime startTime) {
        this.uniqueId = uniqueId;
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "CallSession{" +
                "uniqueId='" + uniqueId + '\'' +
                ", startTime=" + startTime +
                ", callerId='" + callerId + '\'' +
                ", selectedOption='" + selectedOption + '\'' +
                ", validated=" + validated +
                '}';
    }
}
