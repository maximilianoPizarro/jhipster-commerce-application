<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="jhipsterCommerceApp.productOrder.home.createOrEditLabel"
          data-cy="ProductOrderCreateUpdateHeading"
          v-text="t$('jhipsterCommerceApp.productOrder.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="productOrder.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="productOrder.id" readonly />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('jhipsterCommerceApp.productOrder.placedDate')"
              for="product-order-placedDate"
            ></label>
            <div class="d-flex">
              <input
                id="product-order-placedDate"
                data-cy="placedDate"
                type="datetime-local"
                class="form-control"
                name="placedDate"
                :class="{ valid: !v$.placedDate.$invalid, invalid: v$.placedDate.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.placedDate.$model)"
                @change="updateInstantField('placedDate', $event)"
              />
            </div>
            <div v-if="v$.placedDate.$anyDirty && v$.placedDate.$invalid">
              <small class="form-text text-danger" v-for="error of v$.placedDate.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('jhipsterCommerceApp.productOrder.status')" for="product-order-status"></label>
            <select
              class="form-control"
              name="status"
              :class="{ valid: !v$.status.$invalid, invalid: v$.status.$invalid }"
              v-model="v$.status.$model"
              id="product-order-status"
              data-cy="status"
              required
            >
              <option
                v-for="orderStatus in orderStatusValues"
                :key="orderStatus"
                :value="orderStatus"
                :label="t$('jhipsterCommerceApp.OrderStatus.' + orderStatus)"
              >
                {{ orderStatus }}
              </option>
            </select>
            <div v-if="v$.status.$anyDirty && v$.status.$invalid">
              <small class="form-text text-danger" v-for="error of v$.status.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('jhipsterCommerceApp.productOrder.code')" for="product-order-code"></label>
            <input
              type="text"
              class="form-control"
              name="code"
              id="product-order-code"
              data-cy="code"
              :class="{ valid: !v$.code.$invalid, invalid: v$.code.$invalid }"
              v-model="v$.code.$model"
              required
            />
            <div v-if="v$.code.$anyDirty && v$.code.$invalid">
              <small class="form-text text-danger" v-for="error of v$.code.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('jhipsterCommerceApp.productOrder.invoiceId')"
              for="product-order-invoiceId"
            ></label>
            <input
              type="number"
              class="form-control"
              name="invoiceId"
              id="product-order-invoiceId"
              data-cy="invoiceId"
              :class="{ valid: !v$.invoiceId.$invalid, invalid: v$.invoiceId.$invalid }"
              v-model.number="v$.invoiceId.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('jhipsterCommerceApp.productOrder.customer')" for="product-order-customer"></label>
            <select
              class="form-control"
              id="product-order-customer"
              data-cy="customer"
              name="customer"
              v-model="productOrder.customer"
              required
            >
              <option v-if="!productOrder.customer" :value="null" selected></option>
              <option
                :value="productOrder.customer && customerOption.id === productOrder.customer.id ? productOrder.customer : customerOption"
                v-for="customerOption in customers"
                :key="customerOption.id"
              >
                {{ customerOption.email }}
              </option>
            </select>
          </div>
          <div v-if="v$.customer.$anyDirty && v$.customer.$invalid">
            <small class="form-text text-danger" v-for="error of v$.customer.$errors" :key="error.$uid">{{ error.$message }}</small>
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary" @click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.cancel')"></span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="v$.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./product-order-update.component.ts"></script>
