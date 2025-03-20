import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { BudgetDetailComponent } from './budget-detail.component';

describe('Budget Management Detail Component', () => {
  let comp: BudgetDetailComponent;
  let fixture: ComponentFixture<BudgetDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BudgetDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./budget-detail.component').then(m => m.BudgetDetailComponent),
              resolve: { budget: () => of({ id: 5320 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(BudgetDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BudgetDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load budget on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', BudgetDetailComponent);

      // THEN
      expect(instance.budget()).toEqual(expect.objectContaining({ id: 5320 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
