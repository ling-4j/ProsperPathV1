import { Pipe, PipeTransform } from '@angular/core';

import dayjs from 'dayjs/esm';

@Pipe({
  name: 'formatMediumDatetime',
})
export default class FormatMediumDatetimePipe implements PipeTransform {
  transform(day: dayjs.Dayjs | null | undefined): string {
    return day ? day.format('DD/MM/YYYY HH:mm:ss') : '';
  }
}
