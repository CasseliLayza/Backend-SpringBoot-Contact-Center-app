package com.backend.clients.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50)
    private String name;
    @Column(unique = true, length = 50)
    private String dni;
    @Column(length = 50)
    private String phone;
    @Column(length = 50)
    private String email;
    @Column(length = 50)
    private String address;
    @Column(length = 50)
    private String city;
    @Column(length = 50)
    private String balance;

    private Boolean isActive;

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dni='" + dni + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", balance='" + balance + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
