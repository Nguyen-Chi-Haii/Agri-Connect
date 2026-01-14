package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);

    @org.springframework.data.mongodb.repository.Query("{ '$or': [ { 'fullName': { '$regex': ?0, '$options': 'i' } }, { 'phone': { '$regex': ?0, '$options': 'i' } } ] }")
    java.util.List<User> searchUsers(String keyword);
}
