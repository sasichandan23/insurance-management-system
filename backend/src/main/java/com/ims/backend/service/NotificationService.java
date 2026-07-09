package com.ims.backend.service;

import com.ims.backend.dto.notification.NotificationResponse;
import com.ims.backend.entity.Notification;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.NotificationRepository;
import com.ims.backend.repository.UserRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    /** Creates a notification for the given user. Called by other services on business events. */
    @Transactional
    public void notify(User user, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    /** Notifies every admin account. */
    @Transactional
    public void notifyAdmins(String title, String message) {
        userRepository.findByRole(Role.ADMIN).forEach(admin -> notify(admin, title, message));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> myNotifications(int page, int size) {
        User user = currentUserService.getCurrentUser();
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size))
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long myUnreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(currentUserService.getCurrentUser().getId());
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        User user = currentUserService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not your notification");
        }
        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(currentUserService.getCurrentUser().getId());
    }
}
