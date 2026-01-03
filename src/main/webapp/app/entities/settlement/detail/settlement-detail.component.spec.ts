import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { SettlementDetailComponent } from './settlement-detail.component';

describe('Settlement Management Detail Component', () => {
  let comp: SettlementDetailComponent;
  let fixture: ComponentFixture<SettlementDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SettlementDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./settlement-detail.component').then(m => m.SettlementDetailComponent),
              resolve: { settlement: () => of({ id: 4078 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(SettlementDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SettlementDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load settlement on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SettlementDetailComponent);

      // THEN
      expect(instance.settlement()).toEqual(expect.objectContaining({ id: 4078 }));
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
