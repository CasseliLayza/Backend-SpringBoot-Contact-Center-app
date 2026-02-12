package com.backend.clients.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
@Entity
@Table(name = "calls")
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String telephone;
    private String status;
    private LocalDateTime fecha;


    @Override
    public String toString() {
        return "Call{" +
                "id=" + id +
                ", telephone='" + telephone + '\'' +
                ", status='" + status + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
