package jmc.ex4.services;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import jmc.ex4.dto.UsersDto;
import jmc.ex4.model.Apartment;
import jmc.ex4.model.enums.Role;
import jmc.ex4.model.UserInfo;
import jmc.ex4.repositories.ApartmentRepository;
import jmc.ex4.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApartmentRepository apartmentRepository;
    private final ApartmentService apartmentService;

    /**
     * Loads a user by their email address.
     *
     * @param email the email of the user to load
     * @return UserInfo object containing user details
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserInfo loadUserByUsername(String email) throws UsernameNotFoundException {
        return usersRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Adds a new user to the system.
     * Validates that the email is not already in use and sets the password after encoding it.
     * If the user is an owner, associates them with an apartment if provided.
     *
     * @param userDto the DTO containing user details
     */
    @Transactional
    public void addUser(UsersDto userDto){

        if (usersRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        UserInfo user = new UserInfo(userDto);

        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        if(userDto.getApartmentReferenceCode() != null && !userDto.getApartmentReferenceCode().isBlank()){
            Optional<Apartment> apt = apartmentRepository.findByReferenceCode(userDto.getApartmentReferenceCode());
            if(apt.isPresent()){
                Apartment existingApartment = apt.get();
                if(user.getRole() == Role.OWNER && existingApartment.getOwner() == null) {
                    user.addOwnedApartment(existingApartment);
                }else if(user.getRole() == Role.OWNER){
                    throw new IllegalArgumentException("Apartment already has an owner.");
                }
                else if (user.getRole() == Role.TENANT) {
                    existingApartment.getTenants().add(user);
                    user.setResidence(existingApartment);
                }
            }else{
                throw new IllegalArgumentException("Apartment with the provided reference code does not exist.");
            }
        }

        usersRepository.save(user);
    }

    /**
     * Removes an apartment from the user's ownership.
     * @param userId the ID of the user who owns the apartment
     * @param apartmentId the ID of the apartment to be removed
     */
    @Transactional
    public void removeOwnedApartment(Long userId, Long apartmentId) {
        UserInfo user = usersRepository.findByIdAndRole(userId, Role.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("User not found or not an owner."));

        Apartment apt = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found."));

        if (!user.getOwnedApartments().contains(apt)) {
            throw new IllegalArgumentException("Apartment is not owned by this user.");
        }

        user.getOwnedApartments().remove(apt);
        apt.setOwner(null);
        apartmentService.checkAndDeleteIfOrphan(apt);
    }

    /**
     * Allows a tenant to leave their rented apartment.
     * The user is removed from the apartment's tenant list and their residence is set to null.
     * If the apartment has no tenants left, it will be checked for deletion.
     *
     * @param userId the ID of the user who wants to leave the apartment
     */
    @Transactional
    public void leaveRentedApartment(Long userId) {
        UserInfo user = usersRepository.findByIdAndRole(userId, Role.TENANT)
                .orElseThrow(() -> new IllegalArgumentException("User not found or not a tenant."));

        Apartment apt = user.getResidence();
        if (apt == null) {
            throw new IllegalArgumentException("User is not renting any apartment.");
        }

        apt.getTenants().remove(user);
        user.setResidence(null);

        usersRepository.save(user);
        apartmentRepository.save(apt);

        apartmentService.checkAndDeleteIfOrphan(apt);
    }

    /**
     * Retrieves a user by their ID.
     * Throws an exception if the user does not exist.
     *
     * @param userId the ID of the user to retrieve
     * @return UserInfo object containing user details
     */
    @Transactional(readOnly = true)
    public UserInfo getUserById(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    /**
     * Retrieves a list of apartments owned by a specific user.
     * Throws an exception if the user does not exist or is not an owner.
     *
     * @param userId the ID of the user whose owned apartments are to be retrieved
     * @return List of Apartment objects owned by the user
     */
    @Transactional(readOnly = true)
    public List<Apartment> getUserOwnedApartments(Long userId) {
        UserInfo user = usersRepository.findByIdAndRole(userId, Role.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("User not found or not an owner."));
        List<Apartment> apts = apartmentRepository.findByOwnerId(user.getId());
        return new ArrayList<>(apts) ;
    }
}
