package com.utc.nda.request.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.utc.nda.request.entities.Request;

public interface RequestRepository extends PagingAndSortingRepository<Request, String> , JpaSpecificationExecutor<Request>{
	
	
	long countByStatusIn(List<String> statusList);
	long countByOwnerAndStatusIn(String owner, List<String> statusList);
	long countByOwner(String owner);
	long countByNdaType(String ndaType);
	long countByOwnerAndNdaType(String owner, String ndaType);
	long countByIsAmendment(boolean isAmendment);
	long countByOwnerAndIsAmendment(String owner, boolean isAmendment);
	long countByExpiryDateBetween(Date today, Date aWeekLater);
	long countByOwnerAndExpiryDateBetween(String owner, Date today, Date aWeekLater);
	
	List<Request> findTop3ByOwnerAndCreationTimeBetweenOrderByCreationTimeDesc(String owner,  Date aWeekBefore, Date today);
	List<Request> findTop3ByCreationTimeBetweenOrderByCreationTimeDesc( Date aWeekBefore, Date today);
	
	
	List<Request> findAllByOwnerOrderByCreationTimeDesc(String owner, Pageable p);
	List<Request> findByOrderByCreationTimeDesc(Pageable p);
	List<Request> findAll();
	
	
	List<Request> findByRequestorNameContainingIgnoreCase(String value);
	List<Request> findByCertifierNameContainingIgnoreCase(String value);
	List<Request> findByOtherPartyNameContainingIgnoreCase(String value);
	List<Request> findByBusinessUnitContainingIgnoreCase(String value);
	List<Request> findByOwnerContainingIgnoreCase(String value);
	List<Request> findByInformationSharingPeriod(int value);	
	List<Request> findByCreationTimeBetween(Date start, Date end);
	Optional<Request> findByDocusignGuid(String docusignGuid);
}
