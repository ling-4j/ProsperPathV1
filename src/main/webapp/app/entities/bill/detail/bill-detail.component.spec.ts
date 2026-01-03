import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { BillDetailComponent } from './bill-detail.component';

describe('Bill Management Detail Component', () => {
  let comp: BillDetailComponent;
  let fixture: ComponentFixture<BillDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BillDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./bill-detail.component').then(m => m.BillDetailComponent),
              resolve: { bill: () => of({ id: 8530 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(BillDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BillDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load bill on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', BillDetailComponent);

      // THEN
      expect(instance.bill()).toEqual(expect.objectContaining({ id: 8530 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
