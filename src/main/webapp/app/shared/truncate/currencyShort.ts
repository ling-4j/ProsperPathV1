import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyShort',
  standalone: true,
})
export class CurrencyShortPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value == null) return '';
    const absValue = Math.abs(value);
    if (absValue >= 1_000_000_000) {
      return (value / 1_000_000_000).toFixed(2).replace(/\.00$/, '') + ' tỷ';
    } else if (absValue >= 100_000_000) {
      return (value / 1_000_000).toFixed(2).replace(/\.00$/, '') + ' triệu';
    } else {
      return value.toLocaleString('vi-VN') + 'VND';
    }
  }
}
