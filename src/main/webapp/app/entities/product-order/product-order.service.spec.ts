/* tslint:disable max-line-length */
import axios from 'axios';
import sinon from 'sinon';
import dayjs from 'dayjs';

import ProductOrderService from './product-order.service';
import { DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { ProductOrder } from '@/shared/model/product-order.model';

const error = {
  response: {
    status: null,
    data: {
      type: null,
    },
  },
};

const axiosStub = {
  get: sinon.stub(axios, 'get'),
  post: sinon.stub(axios, 'post'),
  put: sinon.stub(axios, 'put'),
  patch: sinon.stub(axios, 'patch'),
  delete: sinon.stub(axios, 'delete'),
};

describe('Service Tests', () => {
  describe('ProductOrder Service', () => {
    let service: ProductOrderService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new ProductOrderService();
      currentDate = new Date();
      elemDefault = new ProductOrder(123, currentDate, 'COMPLETED', 'AAAAAAA', 0);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = { placedDate: dayjs(currentDate).format(DATE_TIME_FORMAT), ...elemDefault };
        axiosStub.get.resolves({ data: returnedFromService });

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });

      it('should not find an element', async () => {
        axiosStub.get.rejects(error);
        return service
          .find(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should create a ProductOrder', async () => {
        const returnedFromService = { id: 123, placedDate: dayjs(currentDate).format(DATE_TIME_FORMAT), ...elemDefault };
        const expected = { placedDate: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a ProductOrder', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a ProductOrder', async () => {
        const returnedFromService = {
          placedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          status: 'BBBBBB',
          code: 'BBBBBB',
          invoiceId: 1,
          ...elemDefault,
        };

        const expected = { placedDate: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a ProductOrder', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a ProductOrder', async () => {
        const patchObject = { placedDate: dayjs(currentDate).format(DATE_TIME_FORMAT), invoiceId: 1, ...new ProductOrder() };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { placedDate: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a ProductOrder', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of ProductOrder', async () => {
        const returnedFromService = {
          placedDate: dayjs(currentDate).format(DATE_TIME_FORMAT),
          status: 'BBBBBB',
          code: 'BBBBBB',
          invoiceId: 1,
          ...elemDefault,
        };
        const expected = { placedDate: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of ProductOrder', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a ProductOrder', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a ProductOrder', async () => {
        axiosStub.delete.rejects(error);

        return service
          .delete(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });
    });
  });
});
