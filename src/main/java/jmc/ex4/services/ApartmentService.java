package jmc.ex4.services;

import jmc.ex4.utils.ReferenceCodeGenerator;
import org.springframework.transaction.annotation.Transactional;
import jmc.ex4.model.Apartment;
import jmc.ex4.model.enums.Role;
import jmc.ex4.model.UserInfo;
import jmc.ex4.repositories.ApartmentRepository;
import jmc.ex4.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing apartments.
 * Provides methods to create, update, and retrieve apartments and their residents.
 */
@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final UsersRepository usersRepository;
    private final ReferenceCodeGenerator referenceCodeGenerator;
    private final int MAX_REFERENCE_CODE_ATTEMPTS = 10;

    public ApartmentService(ApartmentRepository apartmentRepository, UsersRepository usersRepository,
                            ReferenceCodeGenerator referenceCodeGenerator) {
        this.apartmentRepository = apartmentRepository;
        this.usersRepository = usersRepository;
        this.referenceCodeGenerator = referenceCodeGenerator;
    }

    /**
     * Creates a new apartment as an owner.
     * Validates the owner ID and checks if the apartment address already exists.
     * Generates a unique reference code for the apartment.
     *
     * @param address  The address of the apartment.
     * @param ownerId  The ID of the owner.
     */
    @Transactional
    public void createApartmentAsOwner(String address, Long ownerId) {
        if(ownerId == null) {
            throw new IllegalArgumentException("Owner ID must be provided");
        }
        if(apartmentRepository.existsByAddressIgnoreCase(address)) {
            throw new IllegalArgumentException("Apartment with this address already exists");
        }
        Apartment apt = new Apartment();
        apt.setAddress(address);

        UserInfo owner = usersRepository.findByIdAndRole(ownerId, Role.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found or not an owner"));
        apt.setOwner(owner);

        apt.setReferenceCode(generateApartmentReferenceCode());
        apartmentRepository.save(apt);
    }

    /**
     * Creates a new apartment as a tenant.
     * Validates the tenant ID, checks if the apartment address already exists,
     * and ensures the tenant is not already assigned to an apartment.
     * Generates a unique reference code for the apartment.
     *
     * @param address   The address of the apartment.
     * @param tenantId  The ID of the tenant.
     */
    @Transactional
    public void createApartmentAsTenant(String address, Long tenantId) {
        if(tenantId == null) {
            throw new IllegalArgumentException("Tenant ID must be provided");
        }

        if(apartmentRepository.existsByAddressIgnoreCase(address)) {
            throw new IllegalArgumentException("Apartment with this address already exists");
        }

        UserInfo tenant = usersRepository.findByIdAndRole(tenantId, Role.TENANT)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found or not a tenant"));
        if(tenant.getResidence() != null) {
            throw new IllegalArgumentException("Tenant is already assigned to an apartment");
        }

        Apartment apt = new Apartment();
        apt.setAddress(address);
        apt.setReferenceCode(generateApartmentReferenceCode());

        tenant.setResidence(apt);
        apt.getTenants().add(tenant);

        apartmentRepository.save(apt);


    }

    /**
     * Adds an owner to an existing apartment.
     * Validates the apartment ID and owner ID,
     * ensures the apartment does not already have an owner,
     * and sets the owner for the apartment.
     *
     * @param apartmentId The ID of the apartment to which the owner will be added.
     * @param ownerId The ID of the owner to be added.
     */
    @Transactional
    public void addOwnerToApartment(Long apartmentId, Long ownerId) {
        Apartment apt = apartmentRepository.findById(apartmentId).orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        if(apt.getOwner() != null) {
            throw new IllegalArgumentException("Apartment already has an owner");
        }

        UserInfo owner = usersRepository.findByIdAndRole(ownerId, Role.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found or not an owner"));
        apt.setOwner(owner);
    }

    /**
     * Adds a tenant to an existing apartment.
     * Validates the apartment ID and tenant ID,
     * ensures the tenant is not already assigned to another apartment,
     * and adds the tenant to the apartment.
     *
     * @param apartmentId The ID of the apartment to which the tenant will be added.
     * @param tenantId The ID of the tenant to be added.
     */
    @Transactional
    public void addTenantToApartment(Long apartmentId, Long tenantId) {
        Apartment apt = apartmentRepository.findById(apartmentId).orElseThrow(() -> new IllegalArgumentException("Apartment not found"));

        UserInfo tenant = usersRepository.findByIdAndRole(tenantId, Role.TENANT)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found or not a tenant"));
        apt.getTenants().add(tenant);
        tenant.setResidence(apt);
    }

    /**
     * Checks if an apartment is orphaned (i.e., has no owner and no tenants).
     * If it is orphaned, deletes the apartment.
     * @param apt The apartment to check and potentially delete.
     */
    @Transactional
    public void checkAndDeleteIfOrphan(Apartment apt) {
        if (apt.getOwner() == null && apt.getTenants().isEmpty()) {
            apartmentRepository.delete(apt);
        }
    }

    /**
     * Retrieves an apartment by its ID.
     * Throws an exception if the apartment is not found.
     *
     * @param apartmentId The ID of the apartment to retrieve.
     * @return The Apartment object.
     */
    @Transactional(readOnly = true)
    public Apartment getApartmentById(Long apartmentId) {
        return apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found"));
    }

    /**
     * Retrieves a list of residents (tenants) for a given apartment.
     * This method returns all tenants associated with the specified apartment.
     * @param apartmentId The ID of the apartment for which to retrieve residents.
     * @return A list of UserInfo objects representing the tenants of the apartment.
     */
    @Transactional(readOnly = true)
    public List<UserInfo> getApartmentResidents(Long apartmentId) {
        Apartment apt = getApartmentById(apartmentId);
        return new ArrayList<>(apt.getTenants());
    }

    /**
     * Retrieves an apartment by its reference code.
     * Throws an exception if the apartment is not found.
     *
     * @param referenceCode The reference code of the apartment to retrieve.
     * @return The Apartment object.
     */
    @Transactional(readOnly = true)
    public Apartment getApartmentByReferenceCode(String referenceCode) {
        return apartmentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with reference code: " + referenceCode));
    }

    /**
     * Generates a unique reference code for an apartment.
     * It attempts to generate a unique code up to a maximum number of attempts.
     * If it fails to generate a unique code after the maximum attempts, it throws an exception.
     *
     * @return A unique reference code for the apartment.
     */
    private String generateApartmentReferenceCode() {
        int attempts = 0;

        String referenceCode= null;
        do{
            if(attempts++ > MAX_REFERENCE_CODE_ATTEMPTS) {
                throw new IllegalStateException("Failed to generate a unique reference code after " + MAX_REFERENCE_CODE_ATTEMPTS + " attempts");
            }
            referenceCode = referenceCodeGenerator.generateReferenceCode();

        }while(apartmentRepository.existsByReferenceCode(referenceCode));

        return referenceCode;
    }
}
