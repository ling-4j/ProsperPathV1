package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Event;
import com.mycompany.myapp.repository.EventRepository;
import com.mycompany.myapp.service.EventService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Event}.
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    private static final Logger LOG = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event save(Event event) {
        LOG.debug("Request to save Event : {}", event);
        return eventRepository.save(event);
    }

    @Override
    public Event update(Event event) {
        LOG.debug("Request to update Event : {}", event);
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> partialUpdate(Event event) {
        LOG.debug("Request to partially update Event : {}", event);

        return eventRepository
            .findById(event.getId())
            .map(existingEvent -> {
                if (event.getName() != null) {
                    existingEvent.setName(event.getName());
                }
                if (event.getDescription() != null) {
                    existingEvent.setDescription(event.getDescription());
                }
                if (event.getCreatedAt() != null) {
                    existingEvent.setCreatedAt(event.getCreatedAt());
                }

                return existingEvent;
            })
            .map(eventRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findOne(Long id) {
        LOG.debug("Request to get Event : {}", id);
        return eventRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Event : {}", id);
        eventRepository.deleteById(id);
    }
}
