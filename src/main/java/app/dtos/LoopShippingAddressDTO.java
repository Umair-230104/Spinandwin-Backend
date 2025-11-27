package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoopShippingAddressDTO
{
    private String firstName;
    private String lastName;
    private String phone;
    private String address1;
    private String address2;
    private String city;
    private String zip;
    private String countryCode;
}
