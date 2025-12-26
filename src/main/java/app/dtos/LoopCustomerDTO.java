package app.dtos;

import app.entities.Customer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class LoopCustomerDTO
{
    @JsonProperty("id")
    private Long loopCustomerId;                   // loop customer id
    private String firstName;
    private String lastName;
    private String email;
    @JsonAlias({"shopifyId", "customerShopifyId"})
    private Long shopifyId;
    private String phone;

    @JsonAlias({"activeSubscriptions", "activeSubscriptionsCount"})
    private Integer activeSubscriptionsCount;

    @JsonAlias({"allSubscriptionsCount", "allSubscriptions"})
    private Integer allSubscriptionsCount;


    public LoopCustomerDTO(Customer customer)
    {

        this.loopCustomerId = customer.getLoopCustomerId();
        this.shopifyId = customer.getShopifyId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();

    }
}
