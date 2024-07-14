package com.ecommerce.sbecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table( name="users",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
//       username and email columns must have unique values
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String username;

    @NotBlank
    @Size(max=50)
    @Email
    @Column(name = "email")
    private String email;


    @NotBlank
    @Size(max = 120)
    @Column(name = "password")
    private String password;



    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }


    @Getter
    @Setter
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
               fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "user",
                cascade = {CascadeType.PERSIST, CascadeType.MERGE},
                orphanRemoval = true)
// orphanRemoval = true:
// It affects child entities when they are dereferenced from the parent entity, regardless of whether the parent entity is deleted or not.
// If a child entity is removed from the parent's collection or the relationship is set to null, the child entity is deleted from the database.
    private Set<Product> products;

    @ToString.Exclude
    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Cart cart;


    @Getter
    @Setter
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
//    @JoinTable(name = "user_address",
//                joinColumns = @JoinColumn(name = "user_id"),
//                inverseJoinColumns = @JoinColumn(name = "address_id"))
    private List<Address> addresses = new ArrayList<>();


}
