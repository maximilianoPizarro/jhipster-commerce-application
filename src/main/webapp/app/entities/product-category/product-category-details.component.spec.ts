/* tslint:disable max-line-length */
import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import ProductCategoryDetails from './product-category-details.vue';
import ProductCategoryService from './product-category.service';
import AlertService from '@/shared/alert/alert.service';

type ProductCategoryDetailsComponentType = InstanceType<typeof ProductCategoryDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const productCategorySample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('ProductCategory Management Detail Component', () => {
    let productCategoryServiceStub: SinonStubbedInstance<ProductCategoryService>;
    let mountOptions: MountingOptions<ProductCategoryDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      productCategoryServiceStub = sinon.createStubInstance<ProductCategoryService>(ProductCategoryService);

      alertService = new AlertService({
        i18n: { t: vitest.fn() } as any,
        bvToast: {
          toast: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          'font-awesome-icon': true,
          'router-link': true,
        },
        provide: {
          alertService,
          productCategoryService: () => productCategoryServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        productCategoryServiceStub.find.resolves(productCategorySample);
        route = {
          params: {
            productCategoryId: `${123}`,
          },
        };
        const wrapper = shallowMount(ProductCategoryDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.productCategory).toMatchObject(productCategorySample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        productCategoryServiceStub.find.resolves(productCategorySample);
        const wrapper = shallowMount(ProductCategoryDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
