package com.backend.clients.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "calls")
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 50)
    private String uniqueId;
    @Column(length = 50)
    private String channel;
    @Column(length = 50)
    private String optionSelected;
    @Column(length = 50)
    private String result;
    @Column(length = 50)
    private String duration;
    @Column(length = 50)
    private String telephone;
    @Column(length = 50)
    private String status;
    private LocalDateTime dateTime;

    @Override
    public String toString() {
        return "Call{" +
                "id=" + id +
                ", uniqueId='" + uniqueId + '\'' +
                ", channel='" + channel + '\'' +
                ", optionSelected='" + optionSelected + '\'' +
                ", result='" + result + '\'' +
                ", duration='" + duration + '\'' +
                ", telephone='" + telephone + '\'' +
                ", status='" + status + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
