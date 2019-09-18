package com.oracle.coherence.weavesocks.user;

import java.io.Serializable;

import com.oracle.io.pof.annotation.Portable;
import com.oracle.io.pof.annotation.PortableType;

@PortableType(id = 3)
public class AddressDTO implements Serializable {
    @Portable String number;
    @Portable String street;
    @Portable String city;
    @Portable String postcode;
    @Portable String country;

    public AddressDTO() {
    }

    AddressDTO(Address address) {
        this.number = address.number;
        this.street = address.street;
        this.city = address.city;
        this.postcode = address.postcode;
        this.country = address.country;
    }

    @Override
    public String toString() {
        return "AddressDTO{" +
                "number='" + number + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
