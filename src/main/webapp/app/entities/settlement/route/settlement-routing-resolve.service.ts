import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISettlement } from '../settlement.model';
import { SettlementService } from '../service/settlement.service';

const settlementResolve = (route: ActivatedRouteSnapshot): Observable<null | ISettlement> => {
  const id = route.params.id;
  if (id) {
    return inject(SettlementService)
      .find(id)
      .pipe(
        mergeMap((settlement: HttpResponse<ISettlement>) => {
          if (settlement.body) {
            return of(settlement.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default settlementResolve;
