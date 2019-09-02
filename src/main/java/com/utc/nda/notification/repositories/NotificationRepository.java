package com.utc.nda.notification.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.utc.nda.notification.entities.Notification;

public interface NotificationRepository extends PagingAndSortingRepository<Notification, String> {

	List<Notification> findByOwnerOrderByCreationDateDesc(String owner, Pageable p);

	long countByOwner(String owner);
}
