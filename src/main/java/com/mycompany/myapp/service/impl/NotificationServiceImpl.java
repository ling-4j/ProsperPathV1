package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Notification;
import com.mycompany.myapp.repository.NotificationRepository;
import com.mycompany.myapp.service.NotificationService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Notification}.
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification save(Notification notification) {
        LOG.debug("Request to save Notification : {}", notification);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification update(Notification notification) {
        LOG.debug("Request to update Notification : {}", notification);
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> partialUpdate(Notification notification) {
        LOG.debug("Request to partially update Notification : {}", notification);

        return notificationRepository
            .findById(notification.getId())
            .map(existingNotification -> {
                if (notification.getMessage() != null) {
                    existingNotification.setMessage(notification.getMessage());
                }
                if (notification.getNotificationType() != null) {
                    existingNotification.setNotificationType(notification.getNotificationType());
                }
                if (notification.getIsRead() != null) {
                    existingNotification.setIsRead(notification.getIsRead());
                }
                if (notification.getCreatedAt() != null) {
                    existingNotification.setCreatedAt(notification.getCreatedAt());
                }

                return existingNotification;
            })
            .map(notificationRepository::save);
    }

    public Page<Notification> findAllWithEagerRelationships(Pageable pageable) {
        return notificationRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findOne(Long id) {
        LOG.debug("Request to get Notification : {}", id);
        return notificationRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Notification : {}", id);
        notificationRepository.deleteById(id);
    }
}
