<template>
  <div>
    <h2 id="page-heading" data-cy="ProductOrderHeading">
      <span v-text="t$('jhipsterCommerceApp.productOrder.home.title')" id="product-order-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('jhipsterCommerceApp.productOrder.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'ProductOrderCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-product-order"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('jhipsterCommerceApp.productOrder.home.createLabel')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && productOrders && productOrders.length === 0">
      <span v-text="t$('jhipsterCommerceApp.productOrder.home.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="productOrders && productOrders.length > 0">
      <table class="table table-striped" aria-describedby="productOrders">
        <thead>
          <tr>
            <th scope="row" @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('placedDate')">
              <span v-text="t$('jhipsterCommerceApp.productOrder.placedDate')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'placedDate'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('status')">
              <span v-text="t$('jhipsterCommerceApp.productOrder.status')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'status'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('code')">
              <span v-text="t$('jhipsterCommerceApp.productOrder.code')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'code'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('invoiceId')">
              <span v-text="t$('jhipsterCommerceApp.productOrder.invoiceId')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'invoiceId'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('customer.email')">
              <span v-text="t$('jhipsterCommerceApp.productOrder.customer')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'customer.email'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="productOrder in productOrders" :key="productOrder.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'ProductOrderView', params: { productOrderId: productOrder.id } }">{{
                productOrder.id
              }}</router-link>
            </td>
            <td>{{ formatDateShort(productOrder.placedDate) || '' }}</td>
            <td v-text="t$('jhipsterCommerceApp.OrderStatus.' + productOrder.status)"></td>
            <td>{{ productOrder.code }}</td>
            <td>{{ productOrder.invoiceId }}</td>
            <td>
              <div v-if="productOrder.customer">
                <router-link :to="{ name: 'CustomerView', params: { customerId: productOrder.customer.id } }">{{
                  productOrder.customer.email
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'ProductOrderView', params: { productOrderId: productOrder.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'ProductOrderEdit', params: { productOrderId: productOrder.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(productOrder)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline" v-text="t$('entity.action.delete')"></span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <template #modal-title>
        <span
          id="jhipsterCommerceApp.productOrder.delete.question"
          data-cy="productOrderDeleteDialogHeading"
          v-text="t$('entity.delete.title')"
        ></span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-productOrder-heading" v-text="t$('jhipsterCommerceApp.productOrder.delete.question', { id: removeId })"></p>
      </div>
      <template #modal-footer>
        <div>
          <button type="button" class="btn btn-secondary" v-text="t$('entity.action.cancel')" @click="closeDialog()"></button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-productOrder"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entity.action.delete')"
            @click="removeProductOrder()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="productOrders && productOrders.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./product-order.component.ts"></script>
