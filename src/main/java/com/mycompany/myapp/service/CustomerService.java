package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Customer;
import com.mycompany.myapp.repository.CustomerRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Customer}.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Save a customer.
     *
     * @param customer the entity to save.
     * @return the persisted entity.
     */
    public Customer save(Customer customer) {
        LOG.debug("Request to save Customer : {}", customer);
        return customerRepository.save(customer);
    }

    /**
     * Update a customer.
     *
     * @param customer the entity to save.
     * @return the persisted entity.
     */
    public Customer update(Customer customer) {
        LOG.debug("Request to update Customer : {}", customer);
        return customerRepository.save(customer);
    }

    /**
     * Partially update a customer.
     *
     * @param customer the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Customer> partialUpdate(Customer customer) {
        LOG.debug("Request to partially update Customer : {}", customer);

        return customerRepository
            .findById(customer.getId())
            .map(existingCustomer -> {
                if (customer.getFirstName() != null) {
                    existingCustomer.setFirstName(customer.getFirstName());
                }
                if (customer.getLastName() != null) {
                    existingCustomer.setLastName(customer.getLastName());
                }
                if (customer.getGender() != null) {
                    existingCustomer.setGender(customer.getGender());
                }
                if (customer.getEmail() != null) {
                    existingCustomer.setEmail(customer.getEmail());
                }
                if (customer.getPhone() != null) {
                    existingCustomer.setPhone(customer.getPhone());
                }
                if (customer.getAddressLine1() != null) {
                    existingCustomer.setAddressLine1(customer.getAddressLine1());
                }
                if (customer.getAddressLine2() != null) {
                    existingCustomer.setAddressLine2(customer.getAddressLine2());
                }
                if (customer.getCity() != null) {
                    existingCustomer.setCity(customer.getCity());
                }
                if (customer.getCountry() != null) {
                    existingCustomer.setCountry(customer.getCountry());
                }

                return existingCustomer;
            })
            .map(customerRepository::save);
    }

    /**
     * Get all the customers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        LOG.debug("Request to get all Customers");
        return customerRepository.findAll(pageable);
    }

    /**
     * Get all the customers with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<Customer> findAllWithEagerRelationships(Pageable pageable) {
        return customerRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Get one customer by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Customer> findOne(Long id) {
        LOG.debug("Request to get Customer : {}", id);
        return customerRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the customer by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Customer : {}", id);
        customerRepository.deleteById(id);
    }
}
