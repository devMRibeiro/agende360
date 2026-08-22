package br.com.corestacks.agende360.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	@Query("select u from User u where u.id = :id")
	User findByUserId(UUID id);

	User findByEmail(String email);
	
	boolean existsByEmail(String email);
	
	User findByIdAndCompanyId(UUID userId, UUID companyId);
	
	User findByEmailAndCompanyId(String email, UUID companyId);
	
	List<User> findByCompanyId(UUID companyId);
	
	@Query("""
			select u 
			from User u
			inner join Company c on c.id = u.companyId 
			where 
				c.slug = :slug and
				c.active = true and
				u.active = true and
				(u.role = UserRole.PROFESSIONAL or (u.role = UserRole.ADMIN and u.isProfessional = true))
			""")
	List<User> listProfessionals(String slug);
	
	@Modifying
	@Query("update User u set u.isProfessional = :isProfessional where u.id = :userId and u.role = UserRole.ADMIN")
	void toggleProfessionalUser(UUID userId, Boolean isProfessional);

	@Modifying
	@Query("update User u set u.active = :isActive where u.id = :userId")
	void toggleActiveUser(UUID userId, Boolean isActive);

	@Query("select u from User u where u.id = :id and u.companyId = :companyId and u.active = true")
	User findProfessionalById(UUID id, UUID companyId);
}