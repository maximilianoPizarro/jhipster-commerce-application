package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.ProductOrderAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Customer;
import com.mycompany.myapp.domain.ProductOrder;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import com.mycompany.myapp.repository.ProductOrderRepository;
import com.mycompany.myapp.service.ProductOrderService;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProductOrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ProductOrderResourceIT {

    private static final Instant DEFAULT_PLACED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PLACED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.COMPLETED;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.PENDING;

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final Long DEFAULT_INVOICE_ID = 1L;
    private static final Long UPDATED_INVOICE_ID = 2L;

    private static final String ENTITY_API_URL = "/api/product-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Mock
    private ProductOrderRepository productOrderRepositoryMock;

    @Mock
    private ProductOrderService productOrderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductOrderMockMvc;

    private ProductOrder productOrder;

    private ProductOrder insertedProductOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductOrder createEntity(EntityManager em) {
        ProductOrder productOrder = new ProductOrder()
            .placedDate(DEFAULT_PLACED_DATE)
            .status(DEFAULT_STATUS)
            .code(DEFAULT_CODE)
            .invoiceId(DEFAULT_INVOICE_ID);
        // Add required entity
        Customer customer;
        if (TestUtil.findAll(em, Customer.class).isEmpty()) {
            customer = CustomerResourceIT.createEntity(em);
            em.persist(customer);
            em.flush();
        } else {
            customer = TestUtil.findAll(em, Customer.class).get(0);
        }
        productOrder.setCustomer(customer);
        return productOrder;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductOrder createUpdatedEntity(EntityManager em) {
        ProductOrder updatedProductOrder = new ProductOrder()
            .placedDate(UPDATED_PLACED_DATE)
            .status(UPDATED_STATUS)
            .code(UPDATED_CODE)
            .invoiceId(UPDATED_INVOICE_ID);
        // Add required entity
        Customer customer;
        if (TestUtil.findAll(em, Customer.class).isEmpty()) {
            customer = CustomerResourceIT.createUpdatedEntity(em);
            em.persist(customer);
            em.flush();
        } else {
            customer = TestUtil.findAll(em, Customer.class).get(0);
        }
        updatedProductOrder.setCustomer(customer);
        return updatedProductOrder;
    }

    @BeforeEach
    public void initTest() {
        productOrder = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedProductOrder != null) {
            productOrderRepository.delete(insertedProductOrder);
            insertedProductOrder = null;
        }
    }

    @Test
    @Transactional
    void createProductOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProductOrder
        var returnedProductOrder = om.readValue(
            restProductOrderMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productOrder))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProductOrder.class
        );

        // Validate the ProductOrder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertProductOrderUpdatableFieldsEquals(returnedProductOrder, getPersistedProductOrder(returnedProductOrder));

        insertedProductOrder = returnedProductOrder;
    }

    @Test
    @Transactional
    void createProductOrderWithExistingId() throws Exception {
        // Create the ProductOrder with an existing ID
        productOrder.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductOrderMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productOrder)))
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPlacedDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        productOrder.setPlacedDate(null);

        // Create the ProductOrder, which fails.

        restProductOrderMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productOrder)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        productOrder.setStatus(null);

        // Create the ProductOrder, which fails.

        restProductOrderMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productOrder)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        productOrder.setCode(null);

        // Create the ProductOrder, which fails.

        restProductOrderMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productOrder)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProductOrders() throws Exception {
        // Initialize the database
        insertedProductOrder = productOrderRepository.saveAndFlush(productOrder);

        // Get all the productOrderList
        restProductOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].placedDate").value(hasItem(DEFAULT_PLACED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].invoiceId").value(hasItem(DEFAULT_INVOICE_ID.intValue())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProductOrdersWithEagerRelationshipsIsEnabled() throws Exception {
        when(productOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProductOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(productOrderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProductOrdersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(productOrderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProductOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(productOrderRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getProductOrder() throws Exception {
        // Initialize the database
        insertedProductOrder = productOrderRepository.saveAndFlush(productOrder);

        // Get the productOrder
        restProductOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, productOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(productOrder.getId().intValue()))
            .andExpect(jsonPath("$.placedDate").value(DEFAULT_PLACED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.invoiceId").value(DEFAULT_INVOICE_ID.intValue()));
    }

    @Test
    @Transactional
    void getNonExistingProductOrder() throws Exception {
        // Get the productOrder
        restProductOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProductOrder() throws Exception {
        // Initialize the database
        insertedProductOrder = productOrderRepository.saveAndFlush(productOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productOrder
        ProductOrder updatedProductOrder = productOrderRepository.findById(productOrder.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProductOrder are not directly saved in db
        em.detach(updatedProductOrder);
        updatedProductOrder.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).code(UPDATED_CODE).invoiceId(UPDATED_INVOICE_ID);

        restProductOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProductOrder.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedProductOrder))
            )
            .andExpect(status().isOk());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProductOrderToMatchAllProperties(updatedProductOrder);
    }

    @Test
    @Transactional
    void putNonExistingProductOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productOrder.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, productOrder.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(productOrder))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProductOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productOrder.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(productOrder))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProductOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productOrder.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductOrderMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(productOrder)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProductOrderWithPatch() throws Exception {
        // Initialize the database
        insertedProductOrder = productOrderRepository.saveAndFlush(productOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productOrder using partial update
        ProductOrder partialUpdatedProductOrder = new ProductOrder();
        partialUpdatedProductOrder.setId(productOrder.getId());

        partialUpdatedProductOrder.status(UPDATED_STATUS).code(UPDATED_CODE).invoiceId(UPDATED_INVOICE_ID);

        restProductOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductOrder.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProductOrder))
            )
            .andExpect(status().isOk());

        // Validate the ProductOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductOrderUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProductOrder, productOrder),
            getPersistedProductOrder(productOrder)
        );
    }

    @Test
    @Transactional
    void fullUpdateProductOrderWithPatch() throws Exception {
        // Initialize the database
        insertedProductOrder = productOrderRepository.saveAndFlush(productOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productOrder using partial update
        ProductOrder partialUpdatedProductOrder = new ProductOrder();
        partialUpdatedProductOrder.setId(productOrder.getId());

        partialUpdatedProductOrder.placedDate(UPDATED_PLACED_DATE).status(UPDATED_STATUS).code(UPDATED_CODE).invoiceId(UPDATED_INVOICE_ID);

        restProductOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductOrder.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProductOrder))
            )
            .andExpect(status().isOk());

        // Validate the ProductOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductOrderUpdatableFieldsEquals(partialUpdatedProductOrder, getPersistedProductOrder(partialUpdatedProductOrder));
    }

    @Test
    @Transactional
    void patchNonExistingProductOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productOrder.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, productOrder.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(productOrder))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProductOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productOrder.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(productOrder))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProductOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productOrder.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductOrderMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(productOrder))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProductOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProductOrder() throws Exception {
        // Initialize the database
        insertedProductOrder = productOrderRepository.saveAndFlush(productOrder);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the productOrder
        restProductOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, productOrder.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return productOrderRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ProductOrder getPersistedProductOrder(ProductOrder productOrder) {
        return productOrderRepository.findById(productOrder.getId()).orElseThrow();
    }

    protected void assertPersistedProductOrderToMatchAllProperties(ProductOrder expectedProductOrder) {
        assertProductOrderAllPropertiesEquals(expectedProductOrder, getPersistedProductOrder(expectedProductOrder));
    }

    protected void assertPersistedProductOrderToMatchUpdatableProperties(ProductOrder expectedProductOrder) {
        assertProductOrderAllUpdatablePropertiesEquals(expectedProductOrder, getPersistedProductOrder(expectedProductOrder));
    }
}
