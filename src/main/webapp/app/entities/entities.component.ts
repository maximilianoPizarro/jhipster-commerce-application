import { defineComponent, provide } from 'vue';

import ProductService from './product/product.service';
import ProductCategoryService from './product-category/product-category.service';
import CustomerService from './customer/customer.service';
import ProductOrderService from './product-order/product-order.service';
import OrderItemService from './order-item/order-item.service';
import UserService from '@/entities/user/user.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'Entities',
  setup() {
    provide('userService', () => new UserService());
    provide('productService', () => new ProductService());
    provide('productCategoryService', () => new ProductCategoryService());
    provide('customerService', () => new CustomerService());
    provide('productOrderService', () => new ProductOrderService());
    provide('orderItemService', () => new OrderItemService());
    // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
  },
});
