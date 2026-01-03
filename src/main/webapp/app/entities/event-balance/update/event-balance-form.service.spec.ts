import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../event-balance.test-samples';

import { EventBalanceFormService } from './event-balance-form.service';

describe('EventBalance Form Service', () => {
  let service: EventBalanceFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EventBalanceFormService);
  });

  describe('Service methods', () => {
    describe('createEventBalanceFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createEventBalanceFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            paid: expect.any(Object),
            shouldPay: expect.any(Object),
            balance: expect.any(Object),
            event: expect.any(Object),
            member: expect.any(Object),
          }),
        );
      });

      it('passing IEventBalance should create a new form with FormGroup', () => {
        const formGroup = service.createEventBalanceFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            paid: expect.any(Object),
            shouldPay: expect.any(Object),
            balance: expect.any(Object),
            event: expect.any(Object),
            member: expect.any(Object),
          }),
        );
      });
    });

    describe('getEventBalance', () => {
      it('should return NewEventBalance for default EventBalance initial value', () => {
        const formGroup = service.createEventBalanceFormGroup(sampleWithNewData);

        const eventBalance = service.getEventBalance(formGroup) as any;

        expect(eventBalance).toMatchObject(sampleWithNewData);
      });

      it('should return NewEventBalance for empty EventBalance initial value', () => {
        const formGroup = service.createEventBalanceFormGroup();

        const eventBalance = service.getEventBalance(formGroup) as any;

        expect(eventBalance).toMatchObject({});
      });

      it('should return IEventBalance', () => {
        const formGroup = service.createEventBalanceFormGroup(sampleWithRequiredData);

        const eventBalance = service.getEventBalance(formGroup) as any;

        expect(eventBalance).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IEventBalance should not enable id FormControl', () => {
        const formGroup = service.createEventBalanceFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewEventBalance should disable id FormControl', () => {
        const formGroup = service.createEventBalanceFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
