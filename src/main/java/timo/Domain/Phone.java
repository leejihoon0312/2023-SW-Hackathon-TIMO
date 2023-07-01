package timo.Domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
public class Phone {

    @Id
    @Column( name = "phone_number")
    private String phoneNumber;


}
