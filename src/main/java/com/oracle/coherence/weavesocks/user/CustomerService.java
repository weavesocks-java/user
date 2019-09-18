package com.oracle.coherence.weavesocks.user;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import io.helidon.grpc.core.MarshallerSupplier;
import io.helidon.microprofile.grpc.core.GrpcMarshaller;
import io.helidon.microprofile.grpc.core.RpcService;
import io.helidon.microprofile.grpc.core.Unary;

import com.oracle.coherence.helidon.io.PofMarshaller;
import com.oracle.io.pof.PortableTypeSerializer;
import com.oracle.io.pof.SimplePofContext;
import com.oracle.io.pof.annotation.Portable;
import com.oracle.io.pof.annotation.PortableType;
import com.tangosol.net.NamedCache;

import io.grpc.MethodDescriptor;
import io.grpc.Status;

@RpcService
@ApplicationScoped
@GrpcMarshaller("customer")
public class CustomerService {
    private static final Logger LOGGER = Logger.getLogger(CustomerService.class.getName());

    @Inject
    private NamedCache<String, User> users;

    @Unary
    public CustomerResponse getCustomer(CustomerRequest request) {
        LOGGER.info("--> CustomerService.getCustomer: " + request);
        User user = users.get(request.customerId);
        if (user != null) {
            Customer customer = new Customer(user.getId(), user.getFirstName(), user.getLastName());
            Address  address  = user.getAddress(new Address.Id(request.addressId));
            Card     card     = user.getCard(new Card.Id(request.cardId));

            CustomerResponse response = new CustomerResponse(customer, new AddressDTO(address), new CardDTO(card));
            LOGGER.info("<-- CustomerService.getCustomer: " + response);
            return response;
        }
        else {
            throw Status.NOT_FOUND.asRuntimeException();
        }
    }

    // ---- inner class: CustomerRequest ------------------------------------

    @PortableType(id = 1)
    public static class CustomerRequest {
        @Portable String customerId;
        @Portable String addressId;
        @Portable String cardId;

        @Override
        public String toString() {
            return "CustomerRequest{" +
                    "customerId='" + customerId + '\'' +
                    ", addressId='" + addressId + '\'' +
                    ", cardId='" + cardId + '\'' +
                    '}';
        }
    }

    // ---- inner class: CustomerResponse ------------------------------------

    @PortableType(id = 2)
    public static class CustomerResponse {
        @Portable Customer customer;
        @Portable AddressDTO address;
        @Portable CardDTO card;

        CustomerResponse(Customer customer, AddressDTO address, CardDTO card) {
            this.customer = customer;
            this.address = address;
            this.card = card;
        }

        @Override
        public String toString() {
            return "CustomerResponse{" +
                    "customer=" + customer +
                    ", address=" + address +
                    ", card=" + card +
                    '}';
        }
    }

    // ---- inner class: Marshaller -----------------------------------------

    @ApplicationScoped
    @Named("customer")
    public static class Marshaller implements MarshallerSupplier {

        private final MethodDescriptor.Marshaller<?> marshaller;

        @SuppressWarnings("Duplicates")
        public Marshaller() {
            SimplePofContext ctx = new SimplePofContext()
                    .registerPortableType(CustomerRequest.class)
                    .registerPortableType(CustomerResponse.class)
                    .registerPortableType(AddressDTO.class)
                    .registerPortableType(CardDTO.class)
                    .registerPortableType(Customer.class);
            marshaller = new PofMarshaller(ctx);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> MethodDescriptor.Marshaller<T> get(Class<T> aClass) {
            return (MethodDescriptor.Marshaller<T>) marshaller;
        }
    }
}
